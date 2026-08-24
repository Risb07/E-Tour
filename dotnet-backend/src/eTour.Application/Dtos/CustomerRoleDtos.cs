namespace eTour.Application.Dtos;

public class CustomerRequest
{
    /// <summary>The existing User this profile is created for (admin-only endpoint) - never trusted from a self-service caller.</summary>
    public long UserId { get; set; }
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string? Phone { get; set; }
}

public class CustomerResponse
{
    public long CustomerId { get; set; }
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string? Phone { get; set; }
    public long UserId { get; set; }
}

public class RoleDto
{
    public long RoleId { get; set; }
    public string RoleName { get; set; } = null!;
    public string? Description { get; set; }
    public bool Status { get; set; } = true;
}
