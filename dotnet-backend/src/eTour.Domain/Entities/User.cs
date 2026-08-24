namespace eTour.Domain.Entities;

public class User
{
    public long UserId { get; set; }
    public string FirstName { get; set; } = null!;
    public string LastName { get; set; } = null!;
    public string Email { get; set; } = null!;
    /// <summary>BCrypt hash - never serialized to API responses.</summary>
    public string PasswordHash { get; set; } = null!;
    public string? Phone { get; set; }
    public string PreferredLanguage { get; set; } = "en";
    public bool Status { get; set; } = true;

    // ---- Federated sign-in (Google OAuth 2.0) ----
    // All three are nullable and default to local-password behaviour, so every pre-existing row
    // stays valid. The Java backend owns these columns in the shared `users` table; they are
    // mapped here so a Google sign-in through this backend records the same facts it would there.

    /// <summary>"LOCAL" for email/password, "GOOGLE" for an account created via Google sign-in.
    /// Informational only - it never gates a login decision.</summary>
    public string? AuthProvider { get; set; } = "LOCAL";

    /// <summary>Google's immutable subject id. Stored because an email can be reassigned by a
    /// Workspace admin while the sub never changes. Never serialized to API responses.</summary>
    public string? GoogleSub { get; set; }

    /// <summary>Profile picture URL supplied by the provider, if any.</summary>
    public string? AvatarUrl { get; set; }

    public long RoleId { get; set; }
    public Role Role { get; set; } = null!;

    public Customer? Customer { get; set; }
}
