using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/tour-schedules")]
public class TourScheduleController : ControllerBase
{
    private readonly ITourScheduleService _service;

    public TourScheduleController(ITourScheduleService service)
    {
        _service = service;
    }

    [HttpGet("test")]
    [AllowAnonymous]
    public ActionResult<string> Test() => Ok("Controller Working");

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourScheduleDto>>> GetAll(CancellationToken ct) => Ok(await _service.GetAllAsync(ct));

    [HttpGet("{id:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<TourScheduleDto>> GetById(long id, CancellationToken ct) => Ok(await _service.GetByIdAsync(id, ct));

    [HttpGet("tour/{tourId:long}")]
    [AllowAnonymous]
    public async Task<ActionResult<IReadOnlyList<TourScheduleDto>>> GetByTourId(long tourId, CancellationToken ct) =>
        Ok(await _service.GetByTourIdAsync(tourId, ct));

    [HttpPost("tour/{tourId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourScheduleDto>> CreateForTour(long tourId, [FromBody] TourScheduleDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateForTourAsync(tourId, dto, ct));

    [HttpPut("{id:long}/tour/{tourId:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<TourScheduleDto>> Update(long id, long tourId, [FromBody] TourScheduleDto dto, CancellationToken ct) =>
        Ok(await _service.UpdateAsync(id, tourId, dto, ct));

    [HttpDelete("{id:long}")]
    [Authorize(Roles = "ADMIN")]
    public async Task<IActionResult> Delete(long id, CancellationToken ct)
    {
        await _service.DeleteAsync(id, ct);
        return NoContent();
    }
}
