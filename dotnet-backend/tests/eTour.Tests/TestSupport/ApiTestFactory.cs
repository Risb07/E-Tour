using eTour.Infrastructure.Persistence;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace eTour.Tests.TestSupport;

/// <summary>
/// Boots the real ASP.NET Core pipeline (auth, exception middleware, controllers, DI wiring) with
/// the MySQL DbContext swapped for a fresh InMemory one per factory instance, and seeding
/// disabled for fast, deterministic tests.
/// </summary>
public class ApiTestFactory : WebApplicationFactory<eTour.Api.Program>
{
    private readonly string _dbName = Guid.NewGuid().ToString();

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.UseEnvironment("Testing");
        builder.ConfigureAppConfiguration((_, config) =>
        {
            config.AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["Seed:Enabled"] = "false",
                ["Jwt:Secret"] = "test-secret-test-secret-test-secret-32bytes",

                // Pinned empty on purpose. The factory boots the real host, which still reads the
                // developer's appsettings.json, so without this the suite's behaviour would depend
                // on whether that machine happens to have real OAuth credentials in it - the OAuth
                // tests would pass on a clean checkout and fail the moment someone configured
                // Google locally. Tests must never depend on a developer's own secrets.
                ["OAuth:Google:ClientId"] = "",
                ["OAuth:Google:ClientSecret"] = "",
            });
        });

        builder.ConfigureServices(services =>
        {
            // Program.cs skips its own MySQL registration under the "Testing" environment (set
            // above), so this is the only EF Core provider registered for EtourDbContext - no
            // "two providers registered" conflict.
            services.AddDbContext<EtourDbContext>(options => options.UseInMemoryDatabase(_dbName));
        });
    }
}
