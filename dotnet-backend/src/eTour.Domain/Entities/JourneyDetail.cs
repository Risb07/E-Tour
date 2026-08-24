using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class JourneyDetail
{
    public long JourneyId { get; set; }
    public int? SequenceNo { get; set; }
    public string? Notes { get; set; }
    public TravelMode ModeOfTravel { get; set; } = TravelMode.ROAD;

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;

    public long FromLocationId { get; set; }
    public Location FromLocation { get; set; } = null!;

    public long ToLocationId { get; set; }
    public Location ToLocation { get; set; } = null!;
}
