namespace eTour.Application.Dtos;

public class ItineraryDto
{
    public long ItineraryId { get; set; }
    public int DayNumber { get; set; }
    public string Title { get; set; } = null!;
    public string? Description { get; set; }
    public long TourId { get; set; }
}

public class JourneyDetailDto
{
    public long JourneyId { get; set; }
    public int? SequenceNo { get; set; }
    public string? Notes { get; set; }
    public string ModeOfTravel { get; set; } = "ROAD";
    public long TourId { get; set; }
    public long FromLocationId { get; set; }
    public long ToLocationId { get; set; }
    // Resolved names, not just ids: the itinerary's journey legs render "<from> to <to>" and have
    // no separate location lookup to join against.
    public string? FromLocationName { get; set; }
    public string? ToLocationName { get; set; }
}

public class StayMealDto
{
    public long StayMealId { get; set; }
    public int DayNumber { get; set; }
    public string? HotelName { get; set; }
    public bool Breakfast { get; set; }
    public bool Lunch { get; set; }
    public bool Dinner { get; set; }
    public long TourId { get; set; }
    public long? LocationId { get; set; }
    /// <summary>Fallback label for the stay row when no hotel name is set.</summary>
    public string? LocationName { get; set; }
}

public class TourAddonDto
{
    public long AddonId { get; set; }
    public string AddonName { get; set; } = null!;
    public string? Description { get; set; }
    public decimal Price { get; set; }
    public bool IsOptional { get; set; } = true;
    public int DisplayOrder { get; set; }
    public bool Status { get; set; } = true;
    public string PriceType { get; set; } = "PER_PERSON";
    public long TourId { get; set; }
}

public class TourContentDto
{
    public long TourContentId { get; set; }
    public string LanguageCode { get; set; } = "en";
    public string? ContentText { get; set; }
    public bool Status { get; set; } = true;
    public string ContentType { get; set; } = null!;
    public long TourId { get; set; }
}

public class TourMediaDto
{
    public long MediaId { get; set; }
    public string FilePath { get; set; } = null!;
    public string? MimeType { get; set; }
    public int DisplayOrder { get; set; }
    public bool Status { get; set; } = true;
    public string MediaType { get; set; } = null!;
    public string TabContext { get; set; } = "GALLERY";
    public long TourId { get; set; }
}

/// <summary>Aggregate response for the public tour detail page: tour + schedules + itinerary + reviews + review summary.</summary>
public class TourDetailsResponse
{
    public TourResponse Tour { get; set; } = null!;
    public List<TourScheduleDto> Schedules { get; set; } = [];
    public List<ItineraryDto> Itinerary { get; set; } = [];
    public List<TourMediaDto> Media { get; set; } = [];
    public List<TourAddonDto> Addons { get; set; } = [];
    public List<ReviewResponse> Reviews { get; set; } = [];
    public ReviewSummary? ReviewSummary { get; set; }
}
