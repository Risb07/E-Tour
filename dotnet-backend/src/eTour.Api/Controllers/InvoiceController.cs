using eTour.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace eTour.Api.Controllers;

[ApiController]
[Route("api/invoices")]
[Authorize(Roles = "ADMIN,CUSTOMER")]
public class InvoiceController : ControllerBase
{
    private readonly IInvoiceService _service;

    public InvoiceController(IInvoiceService service)
    {
        _service = service;
    }

    [HttpGet("booking/{bookingId:long}")]
    public async Task<IActionResult> GetByBooking(long bookingId, CancellationToken ct) => Ok(await _service.GetByBookingAsync(bookingId, ct));

    [HttpGet("me")]
    public async Task<IActionResult> GetMyInvoices(CancellationToken ct) => Ok(await _service.GetMyInvoicesAsync(ct));

    // BRD 3.7 - printable/downloadable PDF receipt.
    [HttpGet("booking/{bookingId:long}/receipt")]
    public async Task<IActionResult> GetReceipt(long bookingId, CancellationToken ct)
    {
        var pdf = await _service.GetReceiptPdfAsync(bookingId, ct);
        return File(pdf, "application/pdf", $"receipt-{bookingId}.pdf");
    }
}
