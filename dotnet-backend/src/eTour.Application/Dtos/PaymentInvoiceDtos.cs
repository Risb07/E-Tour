namespace eTour.Application.Dtos;

public class PaymentRequest
{
    public long BookingId { get; set; }
    public string PaymentMethod { get; set; } = null!;
}

public class CardPaymentRequest
{
    public long BookingId { get; set; }
    public string PaymentMethod { get; set; } = "CARD";
    public string CardNumber { get; set; } = null!;
    public string CardHolderName { get; set; } = null!;
    public int ExpiryMonth { get; set; }
    public int ExpiryYear { get; set; }
    public string Cvv { get; set; } = null!;
    /// <summary>Sent by the card form's "save this card" checkbox. Accepted and ignored, as in Java -
    /// the simulated gateway stores no card data - but it must bind or the request shape drifts.</summary>
    public bool SaveCard { get; set; }
}

public class PaymentResponse
{
    public PaymentResponse()
    {
    }

    public PaymentResponse(long paymentId, long bookingId, decimal amount, string? paymentMethod, string paymentStatus,
        string? transactionRef, DateTime createdAt)
    {
        PaymentId = paymentId;
        BookingId = bookingId;
        Amount = amount;
        PaymentMethod = paymentMethod;
        PaymentStatus = paymentStatus;
        TransactionRef = transactionRef;
        CreatedAt = createdAt;
    }

    public long PaymentId { get; set; }
    public long BookingId { get; set; }
    public decimal Amount { get; set; }
    public string? PaymentMethod { get; set; }
    /// <summary>PaymentStatus, not Status - matches the Java contract this client was built against.</summary>
    public string PaymentStatus { get; set; } = null!;
    public string? TransactionRef { get; set; }
    public DateTime CreatedAt { get; set; }
    public string? Gateway { get; set; }
    public string? CardSummary { get; set; }
    public DateTime UpdatedAt { get; set; }
    public string? OrderNumber { get; set; }
}

public class PaymentSummaryResponse
{
    public long BookingId { get; set; }
    public string BookingStatus { get; set; } = null!;
    public string? OrderNumber { get; set; }
    public long TourId { get; set; }
    public string TourTitle { get; set; } = null!;
    public DateOnly DepartureDate { get; set; }
    public DateOnly ReturnDate { get; set; }
    public int? NumberOfPassengers { get; set; }
    public List<PassengerDto> Passengers { get; set; } = [];
    public List<BookingAddonResponse> Addons { get; set; } = [];
    public RoomSummary? RoomSummary { get; set; }
    public List<CostBreakdownLine>? Breakdown { get; set; }
    public decimal SubTotal { get; set; }
    public decimal DiscountAmount { get; set; }
    public decimal TaxAmount { get; set; }
    public decimal GstRatePercent { get; set; }
    public decimal GrandTotal { get; set; }
    public bool AlreadyPaid { get; set; }
}

public class InvoiceResponse
{
    public long InvoiceId { get; set; }
    public string InvoiceNumber { get; set; } = null!;
    public DateOnly InvoiceDate { get; set; }
    public decimal SubTotal { get; set; }
    public decimal TaxAmount { get; set; }
    public decimal DiscountAmount { get; set; }
    public decimal TotalAmount { get; set; }
    public string? PdfUrl { get; set; }
    public string InvoiceStatus { get; set; } = null!;
    public long BookingId { get; set; }
    public long PaymentId { get; set; }
    public long CustomerId { get; set; }
}
