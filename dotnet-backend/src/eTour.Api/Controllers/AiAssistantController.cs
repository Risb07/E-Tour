using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>Requirement #3 (Microsoft.Extensions.AI): admin-only tour-description drafting assistant. Never writes the tour - the admin reviews and saves via the normal PUT /api/tours/{id}.</summary>
[ApiController]
[Route("api/ai")]
[Authorize(Roles = "ADMIN")]
public class AiAssistantController : ControllerBase
{
    private readonly ITourAiAssistantService _service;

    public AiAssistantController(ITourAiAssistantService service)
    {
        _service = service;
    }

    [HttpPost("tour-description")]
    public async Task<ActionResult<TourDescriptionAssistResponse>> SuggestDescription(
        [FromBody] TourDescriptionAssistRequest request, CancellationToken ct)
    {
        var suggestion = await _service.SuggestDescriptionAsync(request, ct);
        return Ok(new TourDescriptionAssistResponse { SuggestedDescription = suggestion });
    }
}
