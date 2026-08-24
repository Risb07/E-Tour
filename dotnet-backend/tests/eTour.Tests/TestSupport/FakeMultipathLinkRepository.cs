using eTour.Application.Common;
using eTour.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace eTour.Tests.TestSupport;

/// <summary>
/// Test double: the real MultipathLinkRepository issues raw "INSERT IGNORE" SQL, which the
/// InMemory EF provider used in tests can't execute. This achieves the same effect (idempotent
/// link creation) through the tracked navigation collection instead.
/// </summary>
public class FakeMultipathLinkRepository : IMultipathLinkRepository
{
    private readonly EtourDbContext _context;

    public FakeMultipathLinkRepository(EtourDbContext context)
    {
        _context = context;
    }

    public async Task LinkTourToCategoryAsync(long tourId, long categoryId, CancellationToken ct = default)
    {
        var tour = await _context.Tours.Include(t => t.Categories).FirstAsync(t => t.TourId == tourId, ct);
        if (tour.Categories.All(c => c.CategoryId != categoryId))
        {
            var category = await _context.Categories.FirstAsync(c => c.CategoryId == categoryId, ct);
            tour.Categories.Add(category);
            await _context.SaveChangesAsync(ct);
        }
    }
}
