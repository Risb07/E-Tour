using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class Invoice
{
    public long InvoiceId { get; set; }
    public string InvoiceNumber { get; set; } = null!;
    public DateOnly InvoiceDate { get; set; }
    public decimal SubTotal { get; set; }
    public decimal TaxAmount { get; set; }
    public decimal DiscountAmount { get; set; }
    public decimal TotalAmount { get; set; }
    public string? PdfUrl { get; set; }
    public InvoiceStatus InvoiceStatus { get; set; } = InvoiceStatus.GENERATED;
    public DateTime CreatedAt { get; set; }

    public long BookingId { get; set; }
    public Booking Booking { get; set; } = null!;

    public long PaymentId { get; set; }
    public Payment Payment { get; set; } = null!;

    public long CustomerId { get; set; }
    public Customer Customer { get; set; } = null!;
}
