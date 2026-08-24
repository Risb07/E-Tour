using eTour.Application.Dtos;

namespace eTour.Application.Services;

public interface IPassengerService
{
    Task<PassengerDto> AddPassengerAsync(PassengerDto dto, CancellationToken ct = default);
    Task<PassengerDto> UpdatePassengerAsync(long passengerId, PassengerDto dto, CancellationToken ct = default);
    Task<IReadOnlyList<PassengerDto>> GetPassengersForBookingAsync(long bookingId, CancellationToken ct = default);
    Task DeletePassengerAsync(long passengerId, CancellationToken ct = default);
}
