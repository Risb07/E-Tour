namespace eTour.Application.Services;

/// <summary>Requirement #11: HttpClient-based integration with the existing Java Spring Boot microservice.</summary>
public interface IJavaMicroserviceClient
{
    Task<string> GetDataAsync(string relativePath, CancellationToken ct = default);

    /// <summary>Demo call hitting the Java backend's own /api/test sanity endpoint.</summary>
    Task<string> GetJavaBackendStatusAsync(CancellationToken ct = default);
}
