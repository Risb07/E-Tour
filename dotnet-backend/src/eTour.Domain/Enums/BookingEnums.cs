namespace eTour.Domain.Enums;

public enum BookingStatus
{
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

public enum CartStatus
{
    ACTIVE,
    ABANDONED,
    CONVERTED_TO_BOOKING
}

public enum PaymentStatus
{
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}

public enum InvoiceStatus
{
    GENERATED,
    CANCELLED
}

/// <summary>How a TourAddon's price is applied when quantity/passenger count is factored in.</summary>
public enum PriceType
{
    PER_PERSON,
    PER_ROOM,
    PER_BOOKING
}
