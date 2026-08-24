namespace eTour.Application.Dtos;

public class TourSearchRequest
{
    public string? TourName { get; set; }
    public string? TourCode { get; set; }
    public long? CategoryId { get; set; }
    public decimal? MinPrice { get; set; }
    public decimal? MaxPrice { get; set; }
    public int? MinDuration { get; set; }
    public int? MaxDuration { get; set; }
    public DateOnly? StartDate { get; set; }
    public DateOnly? EndDate { get; set; }
    public int Page { get; set; } = 0;
    public int Size { get; set; } = 12;
    public string SortBy { get; set; } = "tourId";
    public string SortDir { get; set; } = "desc";
}

public class TourSearchResultDto
{
    public long TourId { get; set; }
    public string Title { get; set; } = null!;
    public string? Description { get; set; }
    public int DurationDays { get; set; }
    public decimal BasePrice { get; set; }
    public string? TourCode { get; set; }
    public string? ImageUrl { get; set; }
    public long? ScheduleId { get; set; }
    public DateOnly? DepartureDate { get; set; }
    public DateOnly? ReturnDate { get; set; }
    public int? AvailableSeats { get; set; }
    public decimal? SchedulePrice { get; set; }
    public double AverageRating { get; set; }
    public long TotalReviews { get; set; }
}

/// <summary>
/// Property names deliberately match Spring Data's Page&lt;T&gt; JSON shape (content/totalElements/
/// totalPages/number/size), not .NET naming convention - etour-frontend (which this repo must
/// not modify) was built against the Java backend's actual Page&lt;T&gt; responses and reads those
/// exact keys (e.g. SearchPage.jsx, TourListingPage.jsx). Discovered by an end-to-end browser
/// smoke test against the real frontend: a first attempt at this DTO used ordinary .NET names
/// (Items/TotalCount/Page) and every page that lists tours crashed on `result.content.length`.
/// </summary>
public class PagedResult<T>
{
    public List<T> Content { get; set; } = [];
    public int Number { get; set; }
    public int Size { get; set; }
    public long TotalElements { get; set; }
    public int TotalPages { get; set; }
}
