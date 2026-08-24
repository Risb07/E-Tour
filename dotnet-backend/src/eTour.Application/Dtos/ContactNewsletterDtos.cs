namespace eTour.Application.Dtos;

public class ContactEnquiryRequest
{
    public string Name { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string? Phone { get; set; }
    public string? Subject { get; set; }
    public string Message { get; set; } = null!;
}

public class ContactEnquiryResponse
{
    public long EnquiryId { get; set; }
    public string Name { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string? Phone { get; set; }
    public string? Subject { get; set; }
    public string Message { get; set; } = null!;
    public string Status { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}

public class NewsletterRequest
{
    public string Email { get; set; } = null!;
    public string? Name { get; set; }
}

public class NewsletterResponse
{
    public long SubscriberId { get; set; }
    public string Email { get; set; } = null!;
    public string? Name { get; set; }
    public bool Active { get; set; } = true;
    public DateTime SubscribedAt { get; set; }
    public DateTime? UnsubscribedAt { get; set; }
}
