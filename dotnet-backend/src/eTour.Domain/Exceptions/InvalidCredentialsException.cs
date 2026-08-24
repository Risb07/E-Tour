namespace eTour.Domain.Exceptions;

/// <summary>Thrown on a failed login. Middleware always renders this as "Invalid email or password" - never the raw message - so it can't be used to enumerate valid accounts.</summary>
public class InvalidCredentialsException : Exception
{
    public InvalidCredentialsException() : base("Invalid email or password")
    {
    }
}
