namespace eTour.Application.Dtos;

public class TourRequest
{
    public string Title { get; set; } = null!;
    public string? Description { get; set; }
    public int DurationDays { get; set; }
    public decimal BasePrice { get; set; }
    public string TourCode { get; set; } = null!;
    public string Status { get; set; } = "DRAFT";

    /// <summary>
    /// Category links as a list of objects ({ "categoryId": 3 }), matching the Java backend -
    /// whose controller binds the Tour entity directly, so its @ManyToMany Set&lt;Category&gt; is
    /// what the admin UI posts. Only CategoryId is read; any other properties sent are ignored.
    /// </summary>
    public List<CategoryRef> Categories { get; set; } = [];
}

/// <summary>Write-side reference to an existing category (see <see cref="TourRequest.Categories"/>).</summary>
public class CategoryRef
{
    public long CategoryId { get; set; }
}

public class TourResponse
{
    public long TourId { get; set; }
    public string Title { get; set; } = null!;
    public string? Description { get; set; }
    public int DurationDays { get; set; }
    public decimal BasePrice { get; set; }
    public string TourCode { get; set; } = null!;
    public string Status { get; set; } = null!;

    /// <summary>
    /// Nested category objects, matching the Java backend's serialized Tour entity (its
    /// @JsonIgnoreProperties drops the categories' own parentCategory, so this stays flat).
    /// The admin tour form and the public category badges both read categoryId/categoryName
    /// off these objects, so a bare list of ids would leave both silently empty.
    /// </summary>
    public List<CategorySummaryDto> Categories { get; set; } = [];
}

public class TourCostDto
{
    public long CostId { get; set; }
    public decimal? BasePrice { get; set; }
    public decimal? SinglePersonCost { get; set; }
    public decimal? ExtraPersonCost { get; set; }
    public decimal? ChildWithBedCost { get; set; }
    public decimal? ChildWithoutBedCost { get; set; }
    public DateOnly ValidFrom { get; set; }
    public DateOnly ValidTo { get; set; }
    public int Status { get; set; } = 1;
    public long TourId { get; set; }
}

public class TourScheduleDto
{
    public long ScheduleId { get; set; }
    public DateOnly DepartureDate { get; set; }
    public DateOnly ReturnDate { get; set; }
    public int AvailableSeats { get; set; }
    public decimal Price { get; set; }
    public long TourId { get; set; }
}
