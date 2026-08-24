using eTour.Application.Dtos;

namespace eTour.Application.Services;

/// <summary>
/// The "EmployeeService" analogue for this domain: Tour is the central entity, and this service
/// demonstrates composing the generic CRUD layer (GetAll/GetById/Delete delegate straight to
/// IGenericService&lt;Tour,TourResponse,long&gt;) while layering tour-specific business logic on
/// top for the operations that need it (category linking on create/update).
/// </summary>
public interface ITourService
{
    Task<IReadOnlyList<TourResponse>> GetAllAsync(CancellationToken ct = default);
    Task<TourResponse> GetByIdAsync(long id, CancellationToken ct = default);
    Task<IReadOnlyList<TourResponse>> GetByTourCodeAsync(string code, CancellationToken ct = default);
    Task<TourResponse> CreateAsync(TourRequest request, CancellationToken ct = default);
    Task<TourResponse> UpdateAsync(long id, TourRequest request, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
    Task<TourDetailsResponse> GetTourDetailsAsync(long id, CancellationToken ct = default);
}
