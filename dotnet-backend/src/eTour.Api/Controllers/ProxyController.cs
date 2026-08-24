using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>Requirement #11 demo: forwards to the existing Java Spring Boot backend over HttpClient. Optional interop - the .NET backend does not depend on the Java service being up.</summary>
[ApiController]
[Route("api/proxy")]
[AllowAnonymous]
public class ProxyController : ControllerBase
{
    private readonly IJavaMicroserviceClient _javaClient;

    public ProxyController(IJavaMicroserviceClient javaClient)
    {
        _javaClient = javaClient;
    }

    [HttpGet("java-status")]
    public async Task<IActionResult> GetJavaStatus(CancellationToken ct)
    {
        try
        {
            var data = await _javaClient.GetJavaBackendStatusAsync(ct);
            return Ok(new { reachable = true, response = data });
        }
        catch (Exception ex)
        {
            return StatusCode(StatusCodes.Status503ServiceUnavailable, new { reachable = false, message = $"Java microservice unreachable: {ex.Message}" });
        }
    }
}
