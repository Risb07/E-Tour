namespace eTour.Api.Common;

/// <summary>Writes the standard ApiErrorResponse envelope from contexts where an exception can't be thrown (JwtBearer challenge/forbidden events).</summary>
public static class ApiErrorResponseWriter
{
    public static Task WriteAsync(HttpResponse response, int statusCode, string message)
    {
        response.StatusCode = statusCode;
        response.ContentType = "application/json";
        var body = new ApiErrorResponse(DateTime.UtcNow, statusCode, message, response.HttpContext.Request.Path);
        return response.WriteAsJsonAsync(body);
    }
}
