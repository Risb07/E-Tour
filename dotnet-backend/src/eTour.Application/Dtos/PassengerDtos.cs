namespace eTour.Application.Dtos;

public class PassengerDto
{
    public long PassengerId { get; set; }
    public long BookingId { get; set; }
    public string FullName { get; set; } = null!;
    public string? Gender { get; set; }
    public DateOnly? Dob { get; set; }
    public string? Nationality { get; set; }
    public string? IdProofType { get; set; }
    public string? IdProofNumber { get; set; }
    /// <summary>Nullable to match Java's Boolean wrapper - unspecified is stored as null, not coerced to true.</summary>
    public bool? NeedsExtraBed { get; set; } = true;
    public string? PassengerType { get; set; }
    public string? Occupancy { get; set; }
    public decimal? RoomCharge { get; set; }
    public decimal? PassengerPrice { get; set; }
    public string? AddressLine1 { get; set; }
    public string? AddressLine2 { get; set; }
    public string? City { get; set; }
    public string? State { get; set; }
    public string? Country { get; set; }
    public string? Pincode { get; set; }
}
