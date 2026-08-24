using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IContactService
{
    Task<ContactEnquiryResponse> SubmitAsync(ContactEnquiryRequest request, CancellationToken ct = default);
    Task<IReadOnlyList<ContactEnquiryResponse>> GetAllAsync(CancellationToken ct = default);
    Task<ContactEnquiryResponse> GetByIdAsync(long id, CancellationToken ct = default);
    Task<ContactEnquiryResponse> UpdateStatusAsync(long id, string status, CancellationToken ct = default);
    Task<long> CountAsync(CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
