namespace eTour.Application.Dtos;

public class SectorDto
{
    public long SectorId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public string? IconUrl { get; set; }
    public string? ImageUrl { get; set; }
    public bool Active { get; set; } = true;
    public int SortOrder { get; set; }
    public int? TourCount { get; set; }
}

public class SubSectorDto
{
    public long SubSectorId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public string? IconUrl { get; set; }
    public string? ImageUrl { get; set; }
    public bool Active { get; set; } = true;
    public int SortOrder { get; set; }
    public long SectorId { get; set; }
}

public class TourProductDto
{
    public long ProductId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public string? ImageUrl { get; set; }
    public decimal BaseCost { get; set; }
    public int? DurationDays { get; set; }
    public int? DurationNights { get; set; }
    public string? TourCode { get; set; }
    public DateOnly? StartDate { get; set; }
    public DateOnly? EndDate { get; set; }
    public bool Active { get; set; } = true;
    public int SortOrder { get; set; }
    public long SubSectorId { get; set; }
    public long? TourId { get; set; }
}
