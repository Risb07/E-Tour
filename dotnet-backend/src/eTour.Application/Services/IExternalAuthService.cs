using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IExternalAuthService
{
    /// <summary>
    /// Turns an already-verified external identity into the same <see cref="LoginResponse"/> the
    /// password login returns, linking to an existing account by email or provisioning a new
    /// CUSTOMER on first sign-in.
    /// </summary>
    Task<LoginResponse> SignInAsync(ExternalIdentity identity, CancellationToken ct = default);
}
