namespace eTour.Api.Auth;

/// <summary>
/// Names and config keys for the OAuth sign-in flow.
///
/// The cookie scheme here exists ONLY to carry the provider's result across the redirect back
/// from Google (OAuth handlers need a SignInScheme to hand the external principal to). It is
/// never the default authenticate/challenge scheme - JWT bearer stays the default for the whole
/// API - and the callback deletes the cookie the moment it has read it, so no part of the app
/// ever authenticates a normal request from a cookie.
/// </summary>
public static class ExternalAuthDefaults
{
    /// <summary>Short-lived cookie scheme used solely as the OAuth handlers' SignInScheme.</summary>
    public const string ExternalScheme = "eTour.External";

    public const string GoogleScheme = "Google";

    /// <summary>Claim holding the provider's profile-picture URL. Not one of the handler's default
    /// mappings, so Program.cs maps it explicitly; named after the raw Google key it comes from.</summary>
    public const string PictureClaim = "picture";

    public const string GoogleClientIdKey = "OAuth:Google:ClientId";
    public const string GoogleClientSecretKey = "OAuth:Google:ClientSecret";

    /// <summary>
    /// Path Google redirects back to after consent. Whatever this resolves to must be registered
    /// verbatim as an Authorized redirect URI on the Google OAuth client, or Google refuses the
    /// request with "Error 400: redirect_uri_mismatch" before ever reaching this app. Configurable
    /// only so an already-registered URI can be matched without editing the console.
    /// </summary>
    public const string GoogleCallbackPathKey = "OAuth:Google:CallbackPath";

    /// <summary>The framework's default, and what the README tells you to register.</summary>
    public const string DefaultGoogleCallbackPath = "/signin-google";

    public static string GoogleCallbackPath(IConfiguration configuration)
    {
        var configured = configuration[GoogleCallbackPathKey];
        if (string.IsNullOrWhiteSpace(configured))
        {
            return DefaultGoogleCallbackPath;
        }

        // A value missing its leading slash would otherwise throw deep inside the handler with a
        // message that gives no hint which setting was at fault.
        return configured.StartsWith('/')
            ? configured.TrimEnd('/')
            : throw new InvalidOperationException(
                $"{GoogleCallbackPathKey} must start with '/' (e.g. \"{DefaultGoogleCallbackPath}\"), but was \"{configured}\".");
    }

    /// <summary>Where the callback sends the browser once a JWT has been issued.</summary>
    public const string SuccessRedirectKey = "OAuth:SuccessRedirectUrl";
    /// <summary>Where the callback sends the browser when sign-in fails or is cancelled.</summary>
    public const string FailureRedirectKey = "OAuth:FailureRedirectUrl";

    public static bool IsGoogleConfigured(IConfiguration configuration) =>
        !string.IsNullOrWhiteSpace(configuration[GoogleClientIdKey])
        && !string.IsNullOrWhiteSpace(configuration[GoogleClientSecretKey]);
}
