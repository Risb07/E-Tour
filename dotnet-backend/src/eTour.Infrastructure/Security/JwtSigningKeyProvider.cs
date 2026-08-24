using System.Security.Cryptography;
using System.Text;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace eTour.Infrastructure.Security;

/// <summary>
/// Resolves the HS256 signing key exactly like Java's JwtService: a base64 secret that decodes
/// to >= 32 bytes is used as-is; a non-base64/too-short secret is SHA-256 hashed to 32 bytes;
/// an unset secret falls back to a random per-process key (dev-only - invalidates all tokens on
/// restart, logged loudly). Registered as a singleton so token generation and JwtBearer
/// validation always agree on the same key within a process.
/// </summary>
public class JwtSigningKeyProvider
{
    public byte[] Key { get; }

    public JwtSigningKeyProvider(IConfiguration configuration, ILogger<JwtSigningKeyProvider> logger)
    {
        Key = Resolve(configuration["Jwt:Secret"], logger);
    }

    private static byte[] Resolve(string? configuredSecret, ILogger logger)
    {
        if (!string.IsNullOrWhiteSpace(configuredSecret))
        {
            try
            {
                var decoded = Convert.FromBase64String(configuredSecret);
                if (decoded.Length >= 32)
                {
                    return decoded;
                }
            }
            catch (FormatException)
            {
                // Not valid base64 - fall through to the hash fallback below.
            }

            return SHA256.HashData(Encoding.UTF8.GetBytes(configuredSecret));
        }

        logger.LogWarning(
            "Jwt:Secret is not set - using a random per-process signing key. Every restart invalidates " +
            "all issued tokens (everyone gets logged out). Always set Jwt:Secret / JWT_SECRET in production.");
        return RandomNumberGenerator.GetBytes(64);
    }
}
