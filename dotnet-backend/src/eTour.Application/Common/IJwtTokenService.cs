namespace eTour.Application.Common;

public interface IJwtTokenService
{
    /// <summary>Mirrors Java's JwtService: subject = email, iat/exp only - no role/id claims baked in.</summary>
    string GenerateToken(string email);
}
