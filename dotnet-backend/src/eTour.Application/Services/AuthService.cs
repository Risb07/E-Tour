using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;
using Microsoft.Extensions.Logging;

namespace eTour.Application.Services;

public class AuthService : IAuthService
{
    private readonly IGenericRepository<User, long> _userRepository;
    private readonly IJwtTokenService _jwtTokenService;
    private readonly ILogger<AuthService> _logger;

    public AuthService(IGenericRepository<User, long> userRepository, IJwtTokenService jwtTokenService,
        ILogger<AuthService> logger)
    {
        _userRepository = userRepository;
        _jwtTokenService = jwtTokenService;
        _logger = logger;
    }

    public Task<LoginResponse> LoginAsync(LoginRequest request, CancellationToken ct = default)
    {
        var projection = _userRepository.Query()
            .Where(u => u.Email == request.Email)
            .Select(u => new { u.UserId, u.FirstName, u.LastName, u.Email, u.PasswordHash, u.Status, RoleName = u.Role.RoleName })
            .FirstOrDefault();

        // Same message regardless of "no such user" vs "wrong password" - never confirm an
        // email exists. Mirrors Java's BadCredentialsException -> "Invalid email or password".
        if (projection is null || !projection.Status || !BCrypt.Net.BCrypt.Verify(request.Password, projection.PasswordHash))
        {
            _logger.LogInformation("Failed login attempt for {Email}", request.Email);
            throw new InvalidCredentialsException();
        }

        var token = _jwtTokenService.GenerateToken(projection.Email);

        _logger.LogInformation("User {UserId} logged in", projection.UserId);

        return Task.FromResult(new LoginResponse
        {
            Token = token,
            UserId = projection.UserId,
            FirstName = projection.FirstName,
            LastName = projection.LastName,
            Email = projection.Email,
            Role = projection.RoleName
        });
    }
}
