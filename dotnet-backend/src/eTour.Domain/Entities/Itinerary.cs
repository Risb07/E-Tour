namespace eTour.Domain.Entities;

public class Itinerary
{
    public long ItineraryId { get; set; }
    public int DayNumber { get; set; }
    public string Title { get; set; } = null!;
    public string? Description { get; set; }

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
