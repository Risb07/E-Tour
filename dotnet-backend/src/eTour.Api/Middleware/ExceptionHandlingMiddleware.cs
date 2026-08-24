using eTour.Api.Common;
using eTour.Domain.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace eTour.Api.Middleware;

/// <summary>Requirement #4: global exception handling. Maps every ported exception type to one consistent JSON envelope.</summary>
public class ExceptionHandlingMiddleware : IMiddleware
{
    private readonly ILogger<ExceptionHandlingMiddleware> _logger;

    public ExceptionHandlingMiddleware(ILogger<ExceptionHandlingMiddleware> logger)
    {
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context, RequestDelegate next)
    {
        try
        {
            await next(context);
        }
        catch (Exception ex)
        {
            await HandleAsync(context, ex);
        }
    }

    private async Task HandleAsync(HttpContext context, Exception ex)
    {
        var (status, message, logLevel) = Classify(ex);

        _logger.Log(logLevel, ex, "Request {Method} {Path} failed with status {Status}",
            context.Request.Method, context.Request.Path, status);

        if (context.Response.HasStarted)
        {
            return;
        }

        context.Response.ContentType = "application/json";
        context.Response.StatusCode = status;

        var body = new ApiErrorResponse(DateTime.UtcNow, status, message, context.Request.Path);
        await context.Response.WriteAsJsonAsync(body);
    }

    private static (int Status, string Message, LogLevel Level) Classify(Exception ex) => ex switch
    {
        ResourceNotFoundException => (StatusCodes.Status404NotFound, ex.Message, LogLevel.Information),
        InvalidCredentialsException => (StatusCodes.Status401Unauthorized, ex.Message, LogLevel.Information),
        ResourceConflictException => (StatusCodes.Status409Conflict, ex.Message, LogLevel.Information),
        IllegalOperationException => (StatusCodes.Status409Conflict, ex.Message, LogLevel.Information),
        AiNotConfiguredException => (StatusCodes.Status503ServiceUnavailable, ex.Message, LogLevel.Warning),
        ExternalAuthNotConfiguredException => (StatusCodes.Status503ServiceUnavailable, ex.Message, LogLevel.Warning),
        UnauthorizedAccessException => (StatusCodes.Status401Unauthorized, "Authentication is required to perform this action", LogLevel.Information),
        DbUpdateException => (StatusCodes.Status409Conflict, "The operation conflicts with existing data", LogLevel.Error),
        ArgumentException => (StatusCodes.Status400BadRequest, ex.Message, LogLevel.Information),
        _ => (StatusCodes.Status500InternalServerError, "An unexpected error occurred. Please try again later.", LogLevel.Error)
    };
}
