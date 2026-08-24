namespace eTour.Domain.Entities;

public class StayMeal
{
    public long StayMealId { get; set; }
    public int DayNumber { get; set; }
    public string? HotelName { get; set; }
    public bool Breakfast { get; set; }
    public bool Lunch { get; set; }
    public bool Dinner { get; set; }

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;

    public long? LocationId { get; set; }
    public Location? LocationEntity { get; set; }
}
