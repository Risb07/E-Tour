using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface ITourScheduleService
{
    Task<IReadOnlyList<TourScheduleDto>> GetAllAsync(CancellationToken ct = default);
    Task<TourScheduleDto> GetByIdAsync(long id, CancellationToken ct = default);
    Task<IReadOnlyList<TourScheduleDto>> GetByTourIdAsync(long tourId, CancellationToken ct = default);
    Task<TourScheduleDto> CreateForTourAsync(long tourId, TourScheduleDto dto, CancellationToken ct = default);
    Task<TourScheduleDto> UpdateAsync(long scheduleId, long tourId, TourScheduleDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
