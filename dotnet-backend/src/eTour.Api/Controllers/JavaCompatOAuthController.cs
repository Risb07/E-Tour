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
/// Wire-compatibility layer that lets this backend serve the SAME frontend build as the Java
/// backend, with no frontend changes at all.
///
/// <para>
/// Everything else in this API already matches Java's contract - an audit of the frontend's 36
/// endpoints found exactly one route it calls that this backend did not expose
/// (<c>/api/auth/providers</c>), plus a different shape for the Google sign-in handshake. This
/// controller closes both gaps.
/// </para>
///
/// <para><b>What differs between the two backends, and what this maps:</b></para>
/// <list type="table">
///   <item><description>Java starts sign-in at <c>/oauth2/authorization/google</c>; this backend
///   natively uses <c>/api/auth/oauth/google</c>. Both are exposed now.</description></item>
///   <item><description>Java returns the token as a query string to <c>/oauth2/callback</c>; this
///   backend natively returns a URL fragment to <c>/oauth/callback</c>. Both are produced - the
///   native route is untouched, this one adds the Java shape.</description></item>
///   <item><description>Java has <c>/api/auth/providers</c> so the button can hide itself when
///   Google is not configured. Added here.</description></item>
/// </list>
///
/// <para>
/// <b>Nothing in <see cref="OAuthController"/> is modified.</b> That controller keeps its
/// fragment-based flow, which is the better design in isolation - a fragment never reaches a
/// server, so the token stays out of access logs and Referer headers. This controller exists only
/// because the frontend already shipped against Java's query-string shape, and the brief was to
/// switch backends without touching it. If the frontend is ever free to change, prefer the native
/// route and delete this file.
/// </para>
///
/// <para>
/// Set <c>OAuth:Google:CallbackPath</c> to <c>/login/oauth2/code/google</c> (Java's path) and a
/// single Authorized redirect URI in the Google console then works for both backends.
/// </para>
/// </summary>
[ApiController]
[AllowAnonymous]
public class JavaCompatOAuthController : ControllerBase
{
    /// <summary>Where the browser is sent once a JWT has been issued, Java-style query string.</summary>
    public const string CompatSuccessRedirectKey = "OAuth:Compat:SuccessRedirectUrl";

    /// <summary>Where the browser is sent when sign-in fails or is cancelled.</summary>
    public const string CompatFailureRedirectKey = "OAuth:Compat:FailureRedirectUrl";

    /// <summary>The path Java's frontend button navigates to. Kept as a constant so the value
    /// reported by /api/auth/providers and the route below can never drift apart.</summary>
    private const string JavaAuthorizationPath = "/oauth2/authorization/google";

    private readonly IExternalAuthService _externalAuthService;
    private readonly IConfiguration _configuration;
    private readonly ILogger<JavaCompatOAuthController> _logger;

    public JavaCompatOAuthController(IExternalAuthService externalAuthService,
        IConfiguration configuration, ILogger<JavaCompatOAuthController> logger)
    {
        _externalAuthService = externalAuthService;
        _configuration = configuration;
        _logger = logger;
    }

    /// <summary>
    /// Tells the frontend which sign-in methods this deployment has configured, so the
    /// "Continue with Google" button renders only when clicking it would work.
    ///
    /// Mirrors Java's AuthProvidersController exactly, including the property names, because the
    /// frontend reads <c>google</c> off this response. Only booleans and a path are returned -
    /// the client id is never exposed, since the browser has no use for it in a
    /// backend-driven authorization code flow.
    /// </summary>
    [HttpGet("/api/auth/providers")]
    public IActionResult Providers() => Ok(new
    {
        google = ExternalAuthDefaults.IsGoogleConfigured(_configuration),
        googleAuthorizationUrl = JavaAuthorizationPath
    });

    /// <summary>
    /// Java's sign-in entry point. Behaves exactly like
    /// <see cref="OAuthController.StartGoogle"/> but returns to the Java-shaped callback below.
    /// </summary>
    [HttpGet(JavaAuthorizationPath)]
    public IActionResult StartGoogleJavaStyle()
    {
        RequireGoogleConfigured();

        var properties = new AuthenticationProperties
        {
            // Our own origin, not Google's - this is where the Google handler hands control back
            // once it has validated the code and populated the external cookie.
            RedirectUri = Url.Action(nameof(CallbackJavaStyle))!
        };

        return Challenge(properties, ExternalAuthDefaults.GoogleScheme);
    }

    /// <summary>
    /// Issues the JWT and redirects to the frontend with the token and profile as query
    /// parameters - the shape Java's OAuth2AuthenticationSuccessHandler produces and the shape
    /// the existing OAuth2CallbackPage reads.
    ///
    /// As in the native controller there is deliberately no caller-supplied returnUrl: the
    /// redirect carries the token, so honouring an arbitrary one would be an open redirect that
    /// hands the token to whatever host was in the link.
    /// </summary>
    [HttpGet("/api/auth/oauth/google/java-callback")]
    public async Task<IActionResult> CallbackJavaStyle()
    {
        RequireGoogleConfigured();

        var result = await HttpContext.AuthenticateAsync(ExternalAuthDefaults.ExternalScheme);
        if (!result.Succeeded || result.Principal is null)
        {
            _logger.LogInformation("Google sign-in did not complete: {Reason}",
                result.Failure?.Message ?? "no external principal");
            return FailureRedirect("Google sign-in was cancelled or failed.");
        }

        var identity = ToExternalIdentity(result.Principal);

        // The cookie existed only to survive the hop back from Google. Drop it immediately, so the
        // browser leaves holding a JWT and nothing else.
        await HttpContext.SignOutAsync(ExternalAuthDefaults.ExternalScheme);

        LoginResponse login;
        try
        {
            login = await _externalAuthService.SignInAsync(identity, HttpContext.RequestAborted);
        }
        catch (Exception ex) when (ex is InvalidCredentialsException or ArgumentException)
        {
            // A disabled account, or a provider that returned no usable email. The caller is a
            // browser mid-redirect, so send it somewhere renderable rather than letting the
            // exception middleware answer a navigation with JSON.
            _logger.LogInformation("Rejected Google sign-in: {Message}", ex.Message);
            return FailureRedirect(ex.Message);
        }

        var successUrl = _configuration[CompatSuccessRedirectKey];
        if (string.IsNullOrWhiteSpace(successUrl))
        {
            // Not configured: return the same body /api/auth/login returns, so the flow stays
            // usable and testable without a frontend. Same fallback the native controller uses.
            return Ok(login);
        }

        return Redirect(BuildJavaStyleRedirect(successUrl, login));
    }

    /// <summary>
    /// Builds the query string Java produces, parameter for parameter, because the existing
    /// OAuth2CallbackPage reads each of these by name.
    /// </summary>
    private static string BuildJavaStyleRedirect(string baseUrl, LoginResponse login)
    {
        var query = new[]
        {
            $"token={Uri.EscapeDataString(login.Token)}",
            $"userId={login.UserId}",
            $"firstName={Uri.EscapeDataString(login.FirstName ?? string.Empty)}",
            $"lastName={Uri.EscapeDataString(login.LastName ?? string.Empty)}",
            $"email={Uri.EscapeDataString(login.Email ?? string.Empty)}",
            $"role={Uri.EscapeDataString(login.Role ?? string.Empty)}"
        };

        var withoutFragment = baseUrl.Split('#')[0];
        var separator = withoutFragment.Contains('?') ? '&' : '?';
        return $"{withoutFragment}{separator}{string.Join('&', query)}";
    }

    private IActionResult FailureRedirect(string message)
    {
        // Fall back to the native failure URL: both point at the login page, and having one
        // configured but not the other should not turn a cancelled sign-in into a JSON body.
        var failureUrl = _configuration[CompatFailureRedirectKey]
                         ?? _configuration[ExternalAuthDefaults.FailureRedirectKey];

        if (string.IsNullOrWhiteSpace(failureUrl))
        {
            return StatusCode(StatusCodes.Status401Unauthorized,
                new ApiErrorResponse(DateTime.UtcNow, StatusCodes.Status401Unauthorized, message,
                    HttpContext.Request.Path));
        }

        var separator = failureUrl.Contains('?') ? '&' : '?';
        return Redirect($"{failureUrl}{separator}error={Uri.EscapeDataString(message)}");
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
}
