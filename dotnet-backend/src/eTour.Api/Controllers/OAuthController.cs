using System.Security.Claims;
using eTour.Api.Auth;
using eTour.Api.Common;
using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Exceptions;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>
/// OAuth 2.0 social sign-in. Purely additive: /api/auth/login and /api/users/register are
/// untouched, and this route ends by issuing the same eTour JWT they do, so everything
/// downstream (role lookup, [Authorize], ICurrentUserService) is unaffected by which route a
/// user came in through.
///
/// Flow: GET /api/auth/oauth/google  -> 302 to Google
///       Google -> GET /signin-google (handled by the Google handler, sets the external cookie)
///       -> GET /api/auth/oauth/google/callback -> issue JWT -> 302 back to the frontend.
/// </summary>
[ApiController]
[Route("api/auth/oauth")]
[AllowAnonymous]
public class OAuthController : ControllerBase
{
    private readonly IExternalAuthService _externalAuthService;
    private readonly IConfiguration _configuration;
    private readonly ILogger<OAuthController> _logger;

    public OAuthController(IExternalAuthService externalAuthService, IConfiguration configuration,
        ILogger<OAuthController> logger)
    {
        _externalAuthService = externalAuthService;
        _configuration = configuration;
        _logger = logger;
    }

    /// <summary>Starts Google sign-in. Point a "Sign in with Google" link straight at this URL.</summary>
    [HttpGet("google")]
    public IActionResult StartGoogle()
    {
        RequireGoogleConfigured();

        var properties = new AuthenticationProperties
        {
            // Where the Google handler sends the browser after it has validated the code and
            // populated the external cookie - i.e. into Callback below, still on our own origin.
            RedirectUri = Url.Action(nameof(Callback))!
        };

        return Challenge(properties, ExternalAuthDefaults.GoogleScheme);
    }

    // Note there is deliberately no caller-supplied returnUrl on either action. The final redirect
    // carries the JWT, so honouring an arbitrary ?returnUrl= would be an open redirect that hands
    // the token to whatever host an attacker put in the link. The destination is read only from
    // server configuration; if per-client destinations are ever needed, add an allowlist here
    // rather than trusting the query string.
    [HttpGet("google/callback")]
    public async Task<IActionResult> Callback()
    {
        RequireGoogleConfigured();

        var result = await HttpContext.AuthenticateAsync(ExternalAuthDefaults.ExternalScheme);
        if (!result.Succeeded || result.Principal is null)
        {
            // Covers the user cancelling at Google's consent screen as well as a genuine failure.
            _logger.LogInformation("Google sign-in did not complete: {Reason}",
                result.Failure?.Message ?? "no external principal");
            return FailureResult("Google sign-in was cancelled or failed.");
        }

        var identity = ToExternalIdentity(result.Principal);

        // The cookie has served its only purpose (surviving the redirect); drop it immediately so
        // the browser is left holding a JWT and nothing else.
        await HttpContext.SignOutAsync(ExternalAuthDefaults.ExternalScheme);

        LoginResponse login;
        try
        {
            login = await _externalAuthService.SignInAsync(identity, HttpContext.RequestAborted);
        }
        catch (Exception ex) when (ex is InvalidCredentialsException or ArgumentException)
        {
            // A disabled account, or a provider that returned no usable email. The caller here is
            // a browser mid-redirect, so send it somewhere it can render rather than letting the
            // exception middleware answer a navigation with a JSON body.
            _logger.LogInformation("Rejected Google sign-in: {Message}", ex.Message);
            return FailureResult(ex.Message);
        }

        var successUrl = _configuration[ExternalAuthDefaults.SuccessRedirectKey];
        if (string.IsNullOrWhiteSpace(successUrl))
        {
            // No frontend URL configured: hand back the same body /api/auth/login returns, so the
            // flow is fully usable (and testable) before any UI exists for it.
            return Ok(login);
        }

        return Redirect(AppendTokenFragment(successUrl, login.Token));
    }

    private void RequireGoogleConfigured()
    {
        if (!ExternalAuthDefaults.IsGoogleConfigured(_configuration))
        {
            throw new ExternalAuthNotConfiguredException(
                "Google sign-in is not configured on this server. Set OAuth:Google:ClientId and OAuth:Google:ClientSecret.");
        }
    }

    private static ExternalIdentity ToExternalIdentity(ClaimsPrincipal principal) => new()
    {
        Provider = ExternalAuthDefaults.GoogleScheme,
        Subject = principal.FindFirstValue(ClaimTypes.NameIdentifier) ?? string.Empty,
        Email = principal.FindFirstValue(ClaimTypes.Email) ?? string.Empty,
        FirstName = principal.FindFirstValue(ClaimTypes.GivenName),
        LastName = principal.FindFirstValue(ClaimTypes.Surname),
        AvatarUrl = principal.FindFirstValue(ExternalAuthDefaults.PictureClaim)
    };

    /// <summary>
    /// Carries the JWT back in the URL FRAGMENT rather than the query string. A fragment is never
    /// transmitted to a server, so the token stays out of web-server access logs, out of any
    /// proxy in between, and out of the Referer header of whatever the page loads next - all of
    /// which a "?token=" would leak. The receiving page reads it from location.hash.
    /// </summary>
    private static string AppendTokenFragment(string url, string token)
    {
        var withoutFragment = url.Split('#')[0];
        return $"{withoutFragment}#token={Uri.EscapeDataString(token)}";
    }

    private IActionResult FailureResult(string message)
    {
        var failureUrl = _configuration[ExternalAuthDefaults.FailureRedirectKey];
        if (string.IsNullOrWhiteSpace(failureUrl))
        {
            // Same envelope every other failure in this API uses, so a caller without a configured
            // frontend still gets a consistent, parseable error.
            return StatusCode(StatusCodes.Status401Unauthorized,
                new ApiErrorResponse(DateTime.UtcNow, StatusCodes.Status401Unauthorized, message, HttpContext.Request.Path));
        }

        var separator = failureUrl.Contains('?') ? '&' : '?';
        return Redirect($"{failureUrl}{separator}error={Uri.EscapeDataString(message)}");
    }
}
