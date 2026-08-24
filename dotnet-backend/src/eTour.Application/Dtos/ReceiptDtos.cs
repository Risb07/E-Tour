namespace eTour.Application.Dtos;

/// <summary>
/// Everything the PDF/e-mail receipt needs, pre-projected by the caller. Kept separate from the
/// EF entities so generating a receipt never depends on which navigations happen to be loaded.
/// </summary>
public class ReceiptData
{
    public string InvoiceNumber { get; set; } = null!;
    public DateOnly InvoiceDate { get; set; }
    public string? OrderNumber { get; set; }
    public string CustomerFullName { get; set; } = null!;
    public string CustomerEmail { get; set; } = null!;
    public string TourTitle { get; set; } = null!;
    public DateOnly DepartureDate { get; set; }
    public DateOnly ReturnDate { get; set; }
    public int? NumberOfPassengers { get; set; }
    public decimal SubTotal { get; set; }
    public decimal TaxAmount { get; set; }
    public decimal DiscountAmount { get; set; }
    public decimal TotalAmount { get; set; }
    public List<ReceiptPassengerLine> Passengers { get; set; } = [];
}

public class ReceiptPassengerLine
{
    public string FullName { get; set; } = null!;
    public string? Gender { get; set; }
    public string? IdProofNumber { get; set; }
    public string? AddressLine1 { get; set; }
    public string? AddressLine2 { get; set; }
    public string? City { get; set; }
    public string? State { get; set; }
    public string? Country { get; set; }
    public string? Pincode { get; set; }
}
