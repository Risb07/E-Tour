using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/tour-costs")]
public class TourCostController : ControllerBase
{
    private readonly ITourCostService _service;

    public TourCostController(ITourCostService service)
    {
        _service = service;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourCostDto>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<TourCostDto>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpGet("tour/{tourId:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourCostDto>>> GetByTourId(long tourId, CancellationToken ct) =>
        Ok(await _service.GetByTourIdAsync(tourId, ct));

    [HttpPost("tour/{tourId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourCostDto>> CreateForTour(long tourId, [FromBody] TourCostDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateForTourAsync(tourId, dto, ct));

    [HttpPut("{costId:long}/tour/{tourId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourCostDto>> Update(long costId, long tourId, [FromBody] TourCostDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(costId, tourId, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
