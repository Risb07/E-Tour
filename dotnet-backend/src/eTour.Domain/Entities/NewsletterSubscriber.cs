namespace eTour.Domain.Entities;

public class NewsletterSubscriber
{
    public long SubscriberId { get; set; }
    public string Email { get; set; } = null!;
    public string? Name { get; set; }
    public bool Active { get; set; } = true;
    public DateTime SubscribedAt { get; set; }
    public DateTime? UnsubscribedAt { get; set; }
}
