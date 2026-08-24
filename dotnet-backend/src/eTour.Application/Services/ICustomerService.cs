using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ICustomerService
{
    Task<IReadOnlyList<CustomerResponse>> GetAllAsync(CancellationToken ct = default);
    Task<CustomerResponse> CreateAsync(CustomerRequest request, CancellationToken ct = default);
    Task<CustomerResponse> GetByIdAsync(long id, CancellationToken ct = default);
    Task<CustomerResponse> UpdateAsync(long id, CustomerRequest request, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
    Task<CustomerResponse> GetCurrentAsync(CancellationToken ct = default);
    Task<CustomerResponse> UpdateCurrentAsync(UpdateProfileRequest request, CancellationToken ct = default);
}
