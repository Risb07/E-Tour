using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class Passenger
{
    public long PassengerId { get; set; }
    public string FullName { get; set; } = null!;
    /// <summary>Single-character gender code, matching the Java entity.</summary>
    public string? Gender { get; set; }
    public DateOnly? Dob { get; set; }
    /// <summary>ISO-2 nationality code.</summary>
    public string? Nationality { get; set; }
    public string? IdProofType { get; set; }
    public string? IdProofNumber { get; set; }
    /// <summary>
    /// Nullable to match Java's Boolean wrapper exactly: only meaningful for a CHILD-band
    /// passenger (see TourPricingCalculator/Occupancy.DefaultFor); a booking that doesn't specify
    /// it stores null rather than being coerced to true, so this must stay nullable both in the
    /// C# type and in the database column (no EF HasDefaultValue - see the entity's Fluent config).
    /// </summary>
    public bool? NeedsExtraBed { get; set; } = true;

    /// <summary>Resolved by the pricing engine - null for passengers not yet priced.</summary>
    public PassengerType? PassengerType { get; set; }
    /// <summary>Resolved occupancy category - null for infants and unpriced passengers.</summary>
    public Occupancy? Occupancy { get; set; }
    public decimal? RoomCharge { get; set; }
    public decimal? PassengerPrice { get; set; }

    public string? AddressLine1 { get; set; }
    public string? AddressLine2 { get; set; }
    public string? City { get; set; }
    public string? State { get; set; }
    public string? Country { get; set; }
    public string? Pincode { get; set; }

    public long BookingId { get; set; }
    public Booking Booking { get; set; } = null!;
}
