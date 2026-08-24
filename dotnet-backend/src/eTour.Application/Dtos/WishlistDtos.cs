namespace eTour.Application.Dtos;

public class WishlistItemResponse
{
    public long WishlistItemId { get; set; }
    public long TourId { get; set; }
    public string TourTitle { get; set; } = null!;
    public decimal BasePrice { get; set; }
    // The wishlist cards show a tour-code badge and the duration alongside the price.
    public string? TourCode { get; set; }
    public int? DurationDays { get; set; }
    public DateTime CreatedAt { get; set; }
}
