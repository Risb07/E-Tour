using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class BookingAddon
{
    public long BookingAddonId { get; set; }
    /// <summary>Snapshot of TourAddon.AddonName at booking time.</summary>
    public string AddonName { get; set; } = null!;
    public decimal UnitPrice { get; set; }
    public int Quantity { get; set; }
    public decimal TotalAddonCost { get; set; }
    public PriceType PriceType { get; set; }

    public long BookingId { get; set; }
    public Booking Booking { get; set; } = null!;

    public long AddonId { get; set; }
    public TourAddon Addon { get; set; } = null!;
}
