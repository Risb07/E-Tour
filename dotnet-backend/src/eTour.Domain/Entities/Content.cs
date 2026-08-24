namespace eTour.Domain.Entities;

public class Content
{
    public long ContentId { get; set; }
    public string ContentKey { get; set; } = null!;
    public string? PageName { get; set; }
    public string LanguageCode { get; set; } = "en";
    public string? ContentValue { get; set; }
    public string? MediaUrl { get; set; }
    public string? LinkUrl { get; set; }
    public int DisplayOrder { get; set; }
    public bool Status { get; set; } = true;
}
