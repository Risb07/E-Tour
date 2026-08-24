using eTour.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace eTour.Tests.TestSupport;

/// <summary>Fresh in-memory EtourDbContext per test, so tests never share state or need a real MySQL instance.</summary>
public static class TestDbContextFactory
{
    public static EtourDbContext Create()
    {
        var options = new DbContextOptionsBuilder<EtourDbContext>()
            .UseInMemoryDatabase(Guid.NewGuid().ToString())
            .ConfigureWarnings(w => w.Ignore(Microsoft.EntityFrameworkCore.Diagnostics.InMemoryEventId.TransactionIgnoredWarning))
            .Options;
        return new EtourDbContext(options);
    }
}
