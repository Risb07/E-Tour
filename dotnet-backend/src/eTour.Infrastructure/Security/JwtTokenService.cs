using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using eTour.Application.Common;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;

namespace eTour.Infrastructure.Security;

public class JwtTokenService : IJwtTokenService
{
    private readonly JwtSigningKeyProvider _keyProvider;
    private readonly long _expirationMs;

    public JwtTokenService(JwtSigningKeyProvider keyProvider, IConfiguration configuration)
    {
        _keyProvider = keyProvider;
        _expirationMs = configuration.GetValue<long?>("Jwt:ExpirationMs") ?? 86_400_000;
    }

    public string GenerateToken(string email)
    {
        var now = DateTime.UtcNow;
        var credentials = new SigningCredentials(new SymmetricSecurityKey(_keyProvider.Key), SecurityAlgorithms.HmacSha256);

        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, email),
            new Claim(JwtRegisteredClaimNames.Iat, new DateTimeOffset(now).ToUnixTimeSeconds().ToString(),
                ClaimValueTypes.Integer64)
        };

        var token = new JwtSecurityToken(
            claims: claims,
            expires: now.AddMilliseconds(_expirationMs),
            signingCredentials: credentials);

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}
