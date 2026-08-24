using eTour.Domain.Entities;

namespace eTour.Application.Common;

/// <summary>
/// Pessimistic-lock access for Booking, mirroring Java's BookingRepository.findByIdForUpdate.
/// The lock is held for the whole unit of work (fetch -&gt; mutate -&gt; save -&gt; commit), which is
/// why this is a "do work under lock" callback rather than a plain fetch method - releasing the
/// lock before the caller finishes mutating would defeat the point of taking it.
/// </summary>
public interface IBookingLockRepository
{
    Task<TResult> WithLockAsync<TResult>(long bookingId, Func<Booking?, Task<TResult>> action, CancellationToken ct = default);
}

/// <summary>Pessimistic-lock access for TourSchedule, mirroring Java's TourScheduleRepository.findByIdForUpdate (prevents seat oversell).</summary>
public interface ITourScheduleLockRepository
{
    Task<TResult> WithLockAsync<TResult>(long scheduleId, Func<TourSchedule?, Task<TResult>> action, CancellationToken ct = default);
}
