using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class ContactEnquiry
{
    public long EnquiryId { get; set; }
    public string Name { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string? Phone { get; set; }
    public string? Subject { get; set; }
    public string Message { get; set; } = null!;
    public EnquiryStatus Status { get; set; } = EnquiryStatus.NEW;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}
