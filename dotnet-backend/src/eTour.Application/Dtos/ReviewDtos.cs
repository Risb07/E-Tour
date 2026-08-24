namespace eTour.Application.Dtos;

public class ReviewRequest
{
    public int Rating { get; set; }
    public string? Comment { get; set; }
}

public class ReviewResponse
{
    public long ReviewId { get; set; }
    public int Rating { get; set; }
    public string? Comment { get; set; }
    public long TourId { get; set; }
    public long CustomerId { get; set; }
    public string? CustomerName { get; set; }
}

public class ReviewSummary
{
    public double AverageRating { get; set; }
    /// <summary>TotalReviews, not ReviewCount - the rating stars component reads it by this name.</summary>
    public long TotalReviews { get; set; }
}
