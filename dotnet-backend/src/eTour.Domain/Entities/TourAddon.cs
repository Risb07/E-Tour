using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class TourAddon
{
    public long AddonId { get; set; }
    public string AddonName { get; set; } = null!;
    public string? Description { get; set; }
    public decimal Price { get; set; }
    public bool IsOptional { get; set; } = true;
    public int DisplayOrder { get; set; }
    public bool Status { get; set; } = true;
    public PriceType PriceType { get; set; } = PriceType.PER_PERSON;

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
