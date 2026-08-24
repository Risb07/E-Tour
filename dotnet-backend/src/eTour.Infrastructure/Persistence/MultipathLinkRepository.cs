using eTour.Application.Common;
using Microsoft.EntityFrameworkCore;

namespace eTour.Infrastructure.Persistence;

public class MultipathLinkRepository : IMultipathLinkRepository
{
    private readonly EtourDbContext _context;

    public MultipathLinkRepository(EtourDbContext context)
    {
        _context = context;
    }

    public Task LinkTourToCategoryAsync(long tourId, long categoryId, CancellationToken ct = default) =>
        _context.Database.ExecuteSqlInterpolatedAsync(
            $"INSERT IGNORE INTO tour_category (tour_id, category_id) VALUES ({tourId}, {categoryId})", ct);
}
