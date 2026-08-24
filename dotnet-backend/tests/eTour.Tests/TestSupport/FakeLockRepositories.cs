using eTour.Application.Common;
using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace eTour.Tests.TestSupport;

/// <summary>
/// Test double: the InMemory EF provider doesn't support real transactions/row locks, so these
/// just fetch-then-invoke without any locking semantics - fine for single-threaded unit tests
/// that exercise the business logic inside the callback, not the concurrency guarantee itself.
/// </summary>
public class FakeBookingLockRepository : IBookingLockRepository
{
    private readonly eTour.Infrastructure.Persistence.EtourDbContext _context;

    public FakeBookingLockRepository(eTour.Infrastructure.Persistence.EtourDbContext context)
    {
        _context = context;
    }

    public async Task<TResult> WithLockAsync<TResult>(long bookingId, Func<Booking?, Task<TResult>> action, CancellationToken ct = default)
    {
        var booking = await _context.Bookings.Include(b => b.Customer).Include(b => b.Schedule).ThenInclude(s => s.Tour)
            .FirstOrDefaultAsync(b => b.BookingId == bookingId, ct);
        var result = await action(booking);
        await _context.SaveChangesAsync(ct);
        return result;
    }
}

public class FakeTourScheduleLockRepository : ITourScheduleLockRepository
{
    private readonly eTour.Infrastructure.Persistence.EtourDbContext _context;

    public FakeTourScheduleLockRepository(eTour.Infrastructure.Persistence.EtourDbContext context)
    {
        _context = context;
    }

    public async Task<TResult> WithLockAsync<TResult>(long scheduleId, Func<TourSchedule?, Task<TResult>> action, CancellationToken ct = default)
    {
        var schedule = await _context.TourSchedules.Include(s => s.Tour).FirstOrDefaultAsync(s => s.ScheduleId == scheduleId, ct);
        var result = await action(schedule);
        await _context.SaveChangesAsync(ct);
        return result;
    }
}
