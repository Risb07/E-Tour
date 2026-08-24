namespace eTour.Domain.Exceptions;

/// <summary>Thrown when an AI-backed feature is called but no chat provider API key is configured.</summary>
public class AiNotConfiguredException : Exception
{
    public AiNotConfiguredException(string message) : base(message)
    {
    }
}
