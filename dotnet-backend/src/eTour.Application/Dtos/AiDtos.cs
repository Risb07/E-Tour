namespace eTour.Application.Dtos;

public class TourDescriptionAssistRequest
{
    public string TourTitle { get; set; } = null!;
    public List<string> Bullets { get; set; } = [];
    /// <summary>e.g. "adventurous", "family-friendly", "luxury" - optional, defaults to a neutral marketing tone.</summary>
    public string? Tone { get; set; }
}

public class TourDescriptionAssistResponse
{
    public string SuggestedDescription { get; set; } = null!;
}
