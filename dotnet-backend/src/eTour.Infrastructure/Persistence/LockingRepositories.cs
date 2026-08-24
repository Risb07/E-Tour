using eTour.Application.Common;
using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace eTour.Infrastructure.Persistence;

public class BookingLockRepository : IBookingLockRepository
{
    private readonly EtourDbContext _context;

    public BookingLockRepository(EtourDbContext context)
    {
        _context = context;
    }

    public async Task<TResult> WithLockAsync<TResult>(long bookingId, Func<Booking?, Task<TResult>> action, CancellationToken ct = default)
    {
        await using var transaction = await _context.Database.BeginTransactionAsync(ct);
        try
        {
            var booking = await _context.Bookings
                .FromSqlInterpolated($"SELECT * FROM booking WHERE booking_id = {bookingId} FOR UPDATE")
                .Include(b => b.Customer)
                .Include(b => b.Schedule).ThenInclude(s => s.Tour)
                .AsTracking()
                .FirstOrDefaultAsync(ct);

            var result = await action(booking);
            await _context.SaveChangesAsync(ct);
            await transaction.CommitAsync(ct);
            return result;
        }
        catch
        {
            await transaction.RollbackAsync(ct);
            throw;
        }
    }
}

public class TourScheduleLockRepository : ITourScheduleLockRepository
{
    private readonly EtourDbContext _context;

    public TourScheduleLockRepository(EtourDbContext context)
    {
        _context = context;
    }

    public async Task<TResult> WithLockAsync<TResult>(long scheduleId, Func<TourSchedule?, Task<TResult>> action, CancellationToken ct = default)
    {
        await using var transaction = await _context.Database.BeginTransactionAsync(ct);
        try
        {
            var schedule = await _context.TourSchedules
                .FromSqlInterpolated($"SELECT * FROM tour_schedule WHERE schedule_id = {scheduleId} FOR UPDATE")
                .Include(s => s.Tour)
                .AsTracking()
                .FirstOrDefaultAsync(ct);

            var result = await action(schedule);
            await _context.SaveChangesAsync(ct);
            await transaction.CommitAsync(ct);
            return result;
        }
        catch
        {
            await transaction.RollbackAsync(ct);
            throw;
        }
    }
}
