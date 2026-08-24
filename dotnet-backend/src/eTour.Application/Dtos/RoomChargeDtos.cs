namespace eTour.Application.Dtos;

public class RoomChargeDto
{
    public long RoomChargeId { get; set; }
    public decimal Charge { get; set; } = 0;
    public string? Description { get; set; }
    public bool Active { get; set; } = true;
    public string Occupancy { get; set; } = null!;
    public long TourId { get; set; }
}
