namespace eTour.Domain.Entities;

public class Customer
{
    public long CustomerId { get; set; }
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string? Phone { get; set; }

    public long UserId { get; set; }
    public User User { get; set; } = null!;
}
