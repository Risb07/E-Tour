using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ITourCostService
{
    Task<IReadOnlyList<TourCostDto>> GetAllAsync(CancellationToken ct = default);
    Task<TourCostDto> GetByIdAsync(long id, CancellationToken ct = default);
    Task<IReadOnlyList<TourCostDto>> GetByTourIdAsync(long tourId, CancellationToken ct = default);
    Task<TourCostDto> CreateForTourAsync(long tourId, TourCostDto dto, CancellationToken ct = default);
    Task<TourCostDto> UpdateAsync(long costId, long tourId, TourCostDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
