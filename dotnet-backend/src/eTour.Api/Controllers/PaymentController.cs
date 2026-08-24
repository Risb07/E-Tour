using eTour.Application.Dtos;
using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

/// <summary>Payments for the authenticated customer's own bookings - ownership of the specific booking is re-checked in the service on every call.</summary>
[ApiController]
[Route("api/payments")]
[Authorize(Roles = "ADMIN,CUSTOMER")]
public class PaymentController : ControllerBase
{
    private readonly IPaymentService _service;

    public PaymentController(IPaymentService service)
    {
        _service = service;
    }

    [HttpGet("booking/{bookingId:long}/summary")]
    public async Task<ActionResult<PaymentSummaryResponse>> GetSummary(long bookingId, CancellationToken ct) =>
        Ok(await _service.GetPaymentSummaryAsync(bookingId, ct));

    [HttpPost("card")]
    public async Task<ActionResult<PaymentResponse>> PayWithCard([FromBody] CardPaymentRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.PayWithCardAsync(request, ct));

    [HttpPost]
    public async Task<ActionResult<PaymentResponse>> Pay([FromBody] PaymentRequest request, CancellationToken ct) =>
        StatusCode(StatusCodes.Status201Created, await _service.RecordPaymentAsync(request, ct));

    [HttpGet("{paymentId:long}")]
    public async Task<ActionResult<PaymentResponse>> GetPayment(long paymentId, CancellationToken ct) =>
        Ok(await _service.GetPaymentAsync(paymentId, ct));
}
