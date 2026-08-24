namespace eTour.Domain.Exceptions;

/// <summary>
/// Thrown when an OAuth sign-in route is called but that provider has no client id/secret
/// configured. Surfaces as a 503, matching how a missing AI provider key is handled: the feature
/// is unavailable rather than broken, and the app still starts without any OAuth configuration.
/// </summary>
public class ExternalAuthNotConfiguredException : Exception
{
    public ExternalAuthNotConfiguredException(string message) : base(message)
    {
    }
}
