using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IPaymentService
{
    Task<PaymentResponse> RecordPaymentAsync(PaymentRequest request, CancellationToken ct = default);
    Task<PaymentResponse> PayWithCardAsync(CardPaymentRequest request, CancellationToken ct = default);
    Task<PaymentSummaryResponse> GetPaymentSummaryAsync(long bookingId, CancellationToken ct = default);
    Task<PaymentResponse> GetPaymentAsync(long paymentId, CancellationToken ct = default);
}
