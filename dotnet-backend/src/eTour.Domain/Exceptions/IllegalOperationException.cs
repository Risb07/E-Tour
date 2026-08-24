namespace eTour.Domain.Exceptions;

public class IllegalOperationException : Exception
{
    public IllegalOperationException(string message) : base(message)
    {
    }
}
