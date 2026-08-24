namespace eTour.Domain.Entities;

public class Review
{
    public long ReviewId { get; set; }
    public int Rating { get; set; }
    public string? Comment { get; set; }

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;

    public long CustomerId { get; set; }
    public Customer Customer { get; set; } = null!;
}
