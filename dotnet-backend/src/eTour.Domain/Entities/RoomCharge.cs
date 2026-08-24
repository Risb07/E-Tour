using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class RoomCharge
{
    public long RoomChargeId { get; set; }
    public decimal Charge { get; set; } = 0;
    public string? Description { get; set; }
    public bool Active { get; set; } = true;
    public Occupancy Occupancy { get; set; }

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
