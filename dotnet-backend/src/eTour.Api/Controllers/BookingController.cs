using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Enums;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/bookings")]
[Authorize(Roles = "ADMIN,CUSTOMER")]
public class BookingController : ControllerBase
{
    private readonly IBookingService _service;

    public BookingController(IBookingService service)
    {
        _service = service;
    }

    [HttpPost]
    public async Task<ActionResult<BookingResponse>> CreateBooking([FromBody] BookingRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.CreateBookingAsync(request, ct));

    // BRD 3.7 "Done" pax/cost summary shown before the user hits Pay. Nothing is persisted.
    [HttpPost("quote")]
    public async Task<ActionResult<BookingQuoteResponse>> Quote([FromBody] BookingRequest request, CancellationToken ct) =>
        Ok(await _service.QuoteAsync(request, ct));

    // Cart-originated bookings are created with just a headcount; this attaches real passenger
    // details and recalculates the true total before payment is allowed.
    [HttpPatch("{bookingId:long}/passengers")]
    public async Task<ActionResult<BookingResponse>> FinalizePassengers(long bookingId, [FromBody] List<PassengerInput> passengers, CancellationToken ct) =>
        Ok(await _service.FinalizePassengersAsync(bookingId, passengers, ct));

    [HttpGet("me")]
    public async Task<ActionResult<IReadOnlyList<BookingResponse>>> GetMyBookings(CancellationToken ct) => Ok(await _service.GetMyBookingsAsync(ct));

    [HttpGet("{bookingId:long}")]
    public async Task<ActionResult<BookingResponse>> GetBooking(long bookingId, CancellationToken ct) =>
        Ok(await _service.GetBookingByIdAsync(bookingId, ct));

    [HttpGet]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<IReadOnlyList<BookingResponse>>> GetAllBookings(CancellationToken ct) =>
        Ok(await _service.GetAllBookingsAsync(ct));

    [HttpPatch("{bookingId:long}/status")]
    [Authorize(Roles = "ADMIN")]
    public async Task<ActionResult<BookingResponse>> UpdateStatus(long bookingId, [FromQuery] BookingStatus status, CancellationToken ct) =>
        Ok(await _service.UpdateStatusAsync(bookingId, status, ct));

    [HttpDelete("{bookingId:long}")]
    public async Task<IActionResult> CancelBooking(long bookingId, CancellationToken ct)
    {
        await _service.CancelBookingAsync(bookingId, ct);
        return NoContent();
    }
}
