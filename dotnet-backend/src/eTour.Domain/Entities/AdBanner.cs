namespace eTour.Domain.Entities;

public class AdBanner
{
    public long AdId { get; set; }
    public string Title { get; set; } = null!;
    public string? ImageUrl { get; set; }
    public string? LinkUrl { get; set; }
    public string? Position { get; set; }
    public bool Status { get; set; } = true;
}
