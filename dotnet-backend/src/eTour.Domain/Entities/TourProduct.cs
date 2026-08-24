namespace eTour.Domain.Entities;

public class TourProduct
{
    public long ProductId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public string? ImageUrl { get; set; }
    public decimal BaseCost { get; set; }
    public int? DurationDays { get; set; }
    public int? DurationNights { get; set; }
    /// <summary>Free-text field (not the TourCode enum) - kept as a string to match the Java entity.</summary>
    public string? TourCode { get; set; }
    public DateOnly? StartDate { get; set; }
    public DateOnly? EndDate { get; set; }
    public bool Active { get; set; } = true;
    public int SortOrder { get; set; }

    public long SubSectorId { get; set; }
    public SubSector SubSector { get; set; } = null!;

    /// <summary>Optional link to a bookable Tour, set/cleared via resolveTour().</summary>
    public long? TourId { get; set; }
    public Tour? Tour { get; set; }
}
