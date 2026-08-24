namespace eTour.Domain.Entities;

public class SubSector
{
    public long SubSectorId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public string? IconUrl { get; set; }
    public string? ImageUrl { get; set; }
    public bool Active { get; set; } = true;
    public int SortOrder { get; set; }

    public long SectorId { get; set; }
    public Sector Sector { get; set; } = null!;

    public ICollection<TourProduct> Products { get; set; } = new List<TourProduct>();
}
