using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/auth")]
// [AllowAnonymous] is applied per-action rather than to the whole controller: at class level it
// silently overrides any [Authorize] on an action inside it (ASP0026), which would have left
// Me() below reachable anonymously and relying on ICurrentUserService throwing to produce a 401
// rather than the auth pipeline rejecting the request.
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;
    private readonly ICurrentUserService _currentUserService;

    public AuthController(IAuthService authService, ICurrentUserService currentUserService)
    {
        _authService = authService;
        _currentUserService = currentUserService;
    }

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request, CancellationToken ct) =>
        Ok(await _authService.LoginAsync(request, ct));

    /// <summary>
    /// The profile behind the caller's token. Needed by the OAuth flow: that redirect can only
    /// carry the token itself, so the client has no way to learn who it belongs to (or their
    /// role) without asking. Password login still gets all of this in its own response - this
    /// endpoint adds a second way to obtain it and changes nothing about that path.
    /// </summary>
    [HttpGet("me")]
    [Authorize]
    public async Task<ActionResult<CurrentUserResponse>> Me(CancellationToken ct)
    {
        var user = await _currentUserService.CurrentUserAsync(ct);
        return Ok(new CurrentUserResponse
        {
            UserId = user.UserId,
            FirstName = user.FirstName,
            LastName = user.LastName,
            Email = user.Email,
            Role = user.Role.RoleName
        });
    }
}
