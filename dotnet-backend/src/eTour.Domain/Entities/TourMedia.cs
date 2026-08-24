using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class TourMedia
{
    public long MediaId { get; set; }
    public string FilePath { get; set; } = null!;
    public string? MimeType { get; set; }
    public int DisplayOrder { get; set; }
    public bool Status { get; set; } = true;
    public MediaType MediaType { get; set; }
    public TabContext TabContext { get; set; } = TabContext.GALLERY;

    public long TourId { get; set; }
    public Tour Tour { get; set; } = null!;
}
