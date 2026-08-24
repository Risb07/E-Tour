using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IRoomChargeService
{
    Task<IReadOnlyList<RoomChargeDto>> GetByTourIdAsync(long tourId, CancellationToken ct = default);
    Task<RoomChargeDto> CreateForTourAsync(long tourId, RoomChargeDto dto, CancellationToken ct = default);
    Task<RoomChargeDto> UpdateAsync(long id, RoomChargeDto dto, CancellationToken ct = default);
    Task DeleteAsync(long id, CancellationToken ct = default);
}
