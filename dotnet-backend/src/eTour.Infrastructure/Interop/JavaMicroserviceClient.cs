using eTour.Application.Services;

namespace eTour.Infrastructure.Interop;

/// <summary>
/// Requirement #11 reference pattern: calls the existing Java Spring Boot backend (Backend/) over
/// HttpClient. BaseAddress is configured via AddHttpClient in Program.cs from JavaMicroservice:BaseUrl
/// (default http://localhost:8080). This is an optional interop demo - the .NET backend is otherwise
/// self-sufficient and does not depend on the Java service being up.
/// </summary>
public class JavaMicroserviceClient : IJavaMicroserviceClient
{
    private readonly HttpClient _httpClient;

    public JavaMicroserviceClient(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<string> GetDataAsync(string relativePath, CancellationToken ct = default)
    {
        var response = await _httpClient.GetAsync(relativePath, ct);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadAsStringAsync(ct);
    }

    public Task<string> GetJavaBackendStatusAsync(CancellationToken ct = default) => GetDataAsync("/api/test", ct);
}
