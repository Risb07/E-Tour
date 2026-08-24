using eTour.Application.Dtos;
using eTour.Domain.Enums;

namespace eTour.Application.Services;

public interface IBookingService
{
    Task<BookingResponse> CreateBookingAsync(BookingRequest request, CancellationToken ct = default);
    Task<BookingQuoteResponse> QuoteAsync(BookingRequest request, CancellationToken ct = default);
    Task<BookingResponse> FinalizePassengersAsync(long bookingId, List<PassengerInput> passengers, CancellationToken ct = default);
    Task<BookingResponse> GetBookingByIdAsync(long bookingId, CancellationToken ct = default);
    Task<IReadOnlyList<BookingResponse>> GetAllBookingsAsync(CancellationToken ct = default);
    Task<IReadOnlyList<BookingResponse>> GetMyBookingsAsync(CancellationToken ct = default);
    Task<BookingResponse> UpdateStatusAsync(long bookingId, BookingStatus status, CancellationToken ct = default);
    Task CancelBookingAsync(long bookingId, CancellationToken ct = default);
}
