using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class Cart
{
    public long CartId { get; set; }
    public string? PaxSummary { get; set; }
    public int? AdultCount { get; set; }
    public int? ChildCount { get; set; }
    public decimal EstimatedAmount { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public CartStatus Status { get; set; } = CartStatus.ACTIVE;

    public long CustomerId { get; set; }
    public Customer Customer { get; set; } = null!;

    public long ScheduleId { get; set; }
    public TourSchedule Schedule { get; set; } = null!;

    public ICollection<CartAddon> Addons { get; set; } = new List<CartAddon>();
}
