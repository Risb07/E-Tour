namespace eTour.Application.Dtos;

/// <summary>
/// A user identity already verified by an external OAuth/OIDC provider.
///
/// Deliberately provider-agnostic: the controller translates whatever claims Google (or a future
/// provider) returned into this shape, so <see cref="Services.IExternalAuthService"/> holds the
/// account-linking rules with no dependency on any particular provider or on HTTP at all - which
/// is what makes those rules unit-testable without a live OAuth round trip.
/// </summary>
public class ExternalIdentity
{
    /// <summary>Provider key, e.g. "Google". Used only for logging and error messages.</summary>
    public string Provider { get; set; } = null!;

    /// <summary>The provider's stable user id ("sub"). Stored on the user row, but never used as the
    /// linking key - see ExternalAuthService.</summary>
    public string Subject { get; set; } = null!;

    /// <summary>Verified email from the provider. This is the account-linking key.</summary>
    public string Email { get; set; } = null!;

    public string? FirstName { get; set; }
    public string? LastName { get; set; }

    /// <summary>Profile picture URL from the provider ("picture"), if it supplied one.</summary>
    public string? AvatarUrl { get; set; }
}
