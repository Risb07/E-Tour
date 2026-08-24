using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class TourContent
{
    public long TourContentId { get; set; }
    public string LanguageCode { get; set; } = "en";
    public string? ContentText { get; set; }
    public bool Status { get; set; } = true;
    public TourContentType ContentType { get; set; }

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
