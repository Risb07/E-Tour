using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/passengers")]
[Authorize(Roles = "ADMIN,CUSTOMER")]
public class PassengerController : ControllerBase
{
    private readonly IPassengerService _service;

    public PassengerController(IPassengerService service)
    {
        _service = service;
    }

    [HttpPost]
    public async Task<ActionResult<PassengerDto>> Add([FromBody] PassengerDto dto, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.AddPassengerAsync(dto, ct));

    [HttpPut("{passengerId:long}")]
    public async Task<ActionResult<PassengerDto>> Update(long passengerId, [FromBody] PassengerDto dto, CancellationToken ct) =>
        Ok(await _service.UpdatePassengerAsync(passengerId, dto, ct));

    [HttpGet("booking/{bookingId:long}")]
    public async Task<ActionResult<IReadOnlyList<PassengerDto>>> GetForBooking(long bookingId, CancellationToken ct) =>
        Ok(await _service.GetPassengersForBookingAsync(bookingId, ct));

    [HttpDelete("{passengerId:long}")]
    public async Task<IActionResult> Delete(long passengerId, CancellationToken ct)
    {
        await _service.DeletePassengerAsync(passengerId, ct);
        return NoContent();
    }
}
