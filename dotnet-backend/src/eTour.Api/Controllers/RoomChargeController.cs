using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/room-charges")]
public class RoomChargeController : ControllerBase
{
    private readonly IRoomChargeService _service;

    public RoomChargeController(IRoomChargeService service)
    {
        _service = service;
    }

    [HttpGet("tour/{tourId:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<RoomChargeDto>>> GetByTourId(long tourId, CancellationToken ct) =>
        Ok(await _service.GetByTourIdAsync(tourId, ct));

    [HttpPost("tour/{tourId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<RoomChargeDto>> CreateForTour(long tourId, [FromBody] RoomChargeDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateForTourAsync(tourId, dto, ct));

    [HttpPut("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<RoomChargeDto>> Update(long id, [FromBody] RoomChargeDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
