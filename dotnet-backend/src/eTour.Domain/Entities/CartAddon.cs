namespace eTour.Domain.Entities;

public class CartAddon
{
    public long CartAddonId { get; set; }
    public int Quantity { get; set; }
    public decimal EstimatedCost { get; set; }

    public long CartId { get; set; }
    public Cart Cart { get; set; } = null!;

    public long AddonId { get; set; }
    public TourAddon Addon { get; set; } = null!;
}
