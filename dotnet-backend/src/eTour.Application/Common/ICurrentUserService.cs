using eTour.Domain.Entities;

namespace eTour.Application.Common;

/// <summary>
/// Replicates Java's CurrentUserProvider - the IDOR-prevention linchpin. Every service that
/// needs "who is calling" goes through this rather than trusting an id in the request/path.
/// </summary>
public interface ICurrentUserService
{
    bool IsAuthenticated { get; }
    string? Email { get; }
    bool IsAdmin { get; }

    Task<User> CurrentUserAsync(CancellationToken ct = default);

    /// <summary>Throws if the authenticated principal is an admin with no customer profile.</summary>
    Task<Customer> CurrentCustomerAsync(CancellationToken ct = default);
}
