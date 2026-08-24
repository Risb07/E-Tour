using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class Booking
{
    public long BookingId { get; set; }
    public DateOnly BookingDate { get; set; }
    public decimal TotalAmount { get; set; }
    public string? OrderNumber { get; set; }
    public int? NumberOfPassengers { get; set; }
    public int? AdultCount { get; set; }
    public int? ChildCount { get; set; }
    public BookingStatus BookingStatus { get; set; } = BookingStatus.PENDING;

    public long CustomerId { get; set; }
    public Customer Customer { get; set; } = null!;

    public long ScheduleId { get; set; }
    public TourSchedule Schedule { get; set; } = null!;

    public ICollection<BookingAddon> Addons { get; set; } = new List<BookingAddon>();
    public ICollection<Passenger> Passengers { get; set; } = new List<Passenger>();
    public ICollection<Payment> Payments { get; set; } = new List<Payment>();
}
