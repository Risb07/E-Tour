namespace eTour.Domain.Entities;

public class WishlistItem
{
    public long WishlistItemId { get; set; }
    public DateTime CreatedAt { get; set; }

    public long CustomerId { get; set; }
    public Customer Customer { get; set; } = null!;

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
