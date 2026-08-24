using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class Tour
{
    public long TourId { get; set; }
    public string Title { get; set; } = null!;
    public string? Description { get; set; }
    public int DurationDays { get; set; }
    public decimal BasePrice { get; set; }
    public TourCode TourCode { get; set; }
    public TourStatus Status { get; set; } = TourStatus.DRAFT;

    public ICollection<Category> Categories { get; set; } = new List<Category>();
    public ICollection<TourCost> TourCosts { get; set; } = new List<TourCost>();
    public ICollection<TourSchedule> Schedules { get; set; } = new List<TourSchedule>();
    public ICollection<Itinerary> Itineraries { get; set; } = new List<Itinerary>();
    public ICollection<TourAddon> Addons { get; set; } = new List<TourAddon>();
    public ICollection<TourMedia> Media { get; set; } = new List<TourMedia>();
    public ICollection<TourContent> Contents { get; set; } = new List<TourContent>();
    public ICollection<JourneyDetail> JourneyDetails { get; set; } = new List<JourneyDetail>();
    public ICollection<StayMeal> StayMeals { get; set; } = new List<StayMeal>();
    public ICollection<RoomCharge> RoomCharges { get; set; } = new List<RoomCharge>();
    public ICollection<Review> Reviews { get; set; } = new List<Review>();
}
