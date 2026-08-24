using eTour.Application.Dtos;
using eTour.Domain.Entities;

namespace eTour.Application.Services;

public interface IInvoiceService
{
    /// <summary>Called by PaymentService right after a payment succeeds.</summary>
    Task<Invoice> GenerateAsync(Booking booking, Payment payment, CancellationToken ct = default);
    Task<InvoiceResponse> GetByBookingAsync(long bookingId, CancellationToken ct = default);
    Task<IReadOnlyList<InvoiceResponse>> GetMyInvoicesAsync(CancellationToken ct = default);
    Task<byte[]> GetReceiptPdfAsync(long bookingId, CancellationToken ct = default);

    /// <summary>Projects everything the PDF/e-mail receipt needs for a given invoice, independent of which navigations happen to be loaded on any particular entity instance.</summary>
    Task<ReceiptData> BuildReceiptDataAsync(long invoiceId, CancellationToken ct = default);
}
