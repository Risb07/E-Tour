namespace eTour.Domain.Entities;

public class Sector
{
    public long SectorId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public string? IconUrl { get; set; }
    public string? ImageUrl { get; set; }
    public bool Active { get; set; } = true;
    public int SortOrder { get; set; }
    public int? TourCount { get; set; }

    public ICollection<SubSector> SubSectors { get; set; } = new List<SubSector>();
}
