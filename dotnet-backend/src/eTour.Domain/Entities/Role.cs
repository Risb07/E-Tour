namespace eTour.Domain.Entities;

public class Role
{
    public long RoleId { get; set; }
    public string RoleName { get; set; } = null!;
    public string? Description { get; set; }
    public bool Status { get; set; } = true;

    public ICollection<User> Users { get; set; } = new List<User>();
}
