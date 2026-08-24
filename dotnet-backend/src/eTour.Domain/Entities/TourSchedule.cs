namespace eTour.Domain.Entities;

public class TourSchedule
{
    public long ScheduleId { get; set; }
    public DateOnly DepartureDate { get; set; }
    public DateOnly ReturnDate { get; set; }
    public int AvailableSeats { get; set; }
    public decimal Price { get; set; }

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;

    public ICollection<Booking> Bookings { get; set; } = new List<Booking>();
    public ICollection<Cart> Carts { get; set; } = new List<Cart>();

    /// <summary>Mirrors the Java entity's @PrePersist/@PreUpdate guard - call before saving.</summary>
    public void ValidateDates()
    {
        if (ReturnDate < DepartureDate)
        {
            throw new ArgumentException("Return date must be on or after the departure date");
        }
    }
}
