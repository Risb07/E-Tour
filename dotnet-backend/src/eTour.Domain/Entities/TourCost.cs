namespace eTour.Domain.Entities;

public class TourCost
{
    public long CostId { get; set; }
    public decimal? BasePrice { get; set; }
    public decimal? SinglePersonCost { get; set; }
    public decimal? ExtraPersonCost { get; set; }
    public decimal? ChildWithBedCost { get; set; }
    public decimal? ChildWithoutBedCost { get; set; }
    public DateOnly ValidFrom { get; set; }
    public DateOnly ValidTo { get; set; }
    /// <summary>1 = active, 0 = inactive - kept as an int to match the legacy schema column.</summary>
    public int Status { get; set; } = 1;

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
