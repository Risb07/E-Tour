using eTour.Application.Common;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;

namespace eTour.Tests.TestSupport;

/// <summary>Test double for ICurrentUserService - set CurrentCustomer/CurrentUserEntity/IsAdmin directly instead of going through a real ClaimsPrincipal.</summary>
public class FakeCurrentUserService : ICurrentUserService
{
    public bool IsAuthenticated { get; set; } = true;
    public string? Email { get; set; }
    public bool IsAdmin { get; set; }
    public User? CurrentUserEntity { get; set; }
    public Customer? CurrentCustomerEntity { get; set; }

    public Task<User> CurrentUserAsync(CancellationToken ct = default) =>
        CurrentUserEntity is not null ? Task.FromResult(CurrentUserEntity) : throw new ResourceNotFoundException("No current user configured for test");

    public Task<Customer> CurrentCustomerAsync(CancellationToken ct = default) =>
        CurrentCustomerEntity is not null ? Task.FromResult(CurrentCustomerEntity) : throw new ResourceNotFoundException("No current customer configured for test");
}
