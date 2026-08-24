namespace eTour.Domain.Entities;

public class CrawlingText
{
    public long CrawlingTextId { get; set; }
    public string Text { get; set; } = null!;
    public int SortOrder { get; set; }
    public bool IsActive { get; set; } = true;
}
