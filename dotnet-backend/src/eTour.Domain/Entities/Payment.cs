using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class Payment
{
    public long PaymentId { get; set; }
    public decimal Amount { get; set; }
    public string? PaymentMethod { get; set; }
    public PaymentStatus PaymentStatus { get; set; } = PaymentStatus.PENDING;
    public string? TransactionRef { get; set; }
    public string? Gateway { get; set; }
    /// <summary>Brand + last four only - the PAN/CVV are never persisted.</summary>
    public string? CardSummary { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }

    public long BookingId { get; set; }
    public Booking Booking { get; set; } = null!;
}
