using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace eTour.Infrastructure.Seeding;

/// <summary>
/// Ports Java's DataSeeder/DemoDataSeederService 1:1: idempotent startup seeding of roles, a
/// known-credential admin login, a multipath demo category, and per-tour schedule/itinerary/
/// add-on backfill. Every step is existence-checked before insert, so it's safe to run on every
/// startup (gated behind Seed:Enabled, default true, so it can be turned off in an environment
/// that shouldn't auto-seed).
/// </summary>
public class DemoDataSeeder
{
    private readonly EtourDbContext _context;
    private readonly ILogger<DemoDataSeeder> _logger;

    public DemoDataSeeder(EtourDbContext context, ILogger<DemoDataSeeder> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task SeedAllAsync()
    {
        await SeedRolesAndAdminAsync();
        var multipathTour = await SeedEuropeCategoryAndTagTourAsync();
        await SeedSchedulesForBookableToursAsync();
        await SeedItineraryAsync(multipathTour);
        await SeedAddonsAsync();
        _logger.LogInformation("Demo data seeding completed");
    }

    private async Task SeedRolesAndAdminAsync()
    {
        Role? adminRole;

        if (await _context.Roles.CountAsync() == 0)
        {
            adminRole = new Role { RoleName = "ADMIN", Description = "Back-office administrator", Status = true };
            _context.Roles.Add(adminRole);
            _context.Roles.Add(new Role { RoleName = "CUSTOMER", Description = "Registered traveller", Status = true });
            await _context.SaveChangesAsync();
        }
        else
        {
            adminRole = await _context.Roles.FirstOrDefaultAsync(r => r.RoleName == "ADMIN");
        }

        // A known-credential admin login for manual verification.
        const string seedAdminEmail = "admin.seed@etour.com";
        if (adminRole is not null && !await _context.Users.AnyAsync(u => u.Email == seedAdminEmail))
        {
            var seedAdmin = new User
            {
                FirstName = "Demo",
                LastName = "Admin",
                Email = seedAdminEmail,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("Admin@123"),
                PreferredLanguage = "en",
                Status = true,
                RoleId = adminRole.RoleId
            };
            _context.Users.Add(seedAdmin);
            await _context.SaveChangesAsync();
        }
    }

    /// <summary>
    /// Adds an "Europe" sub-category under "International" (if not already present) and tags one
    /// existing tour with it, so that tour becomes reachable via International -&gt; Europe, via
    /// its existing tag, and via direct search - the same tour, three paths.
    /// </summary>
    private async Task<Tour?> SeedEuropeCategoryAndTagTourAsync()
    {
        var europe = await _context.Categories.FirstOrDefaultAsync(c => c.CategoryName == "Europe");
        if (europe is null)
        {
            var international = await _context.Categories.FirstOrDefaultAsync(c => c.CategoryName == "International");
            if (international is null)
            {
                return null;
            }
            europe = new Category
            {
                CategoryName = "Europe",
                Description = "European destinations",
                ParentCategoryId = international.CategoryId,
                CategoryCode = international.CategoryCode,
                IsFeatured = "N",
                Status = true
            };
            _context.Categories.Add(europe);
            await _context.SaveChangesAsync();
        }

        var londonTour = await _context.Tours.Include(t => t.Categories)
            .FirstOrDefaultAsync(t => t.Title.ToLower() == "london, the seasoned classic");
        if (londonTour is not null && londonTour.Categories.All(c => c.CategoryId != europe.CategoryId))
        {
            londonTour.Categories.Add(europe);
            await _context.SaveChangesAsync();
        }
        return londonTour;
    }

    /// <summary>Every tour needs at least one schedule to be bookable at all.</summary>
    private async Task SeedSchedulesForBookableToursAsync()
    {
        var tours = await _context.Tours.ToListAsync();
        foreach (var tour in tours)
        {
            if (await _context.TourSchedules.AnyAsync(s => s.TourId == tour.TourId))
            {
                continue;
            }
            var departureDate = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(2));
            var duration = tour.DurationDays == 0 ? 7 : tour.DurationDays;
            _context.TourSchedules.Add(new TourSchedule
            {
                TourId = tour.TourId,
                DepartureDate = departureDate,
                ReturnDate = departureDate.AddDays(duration),
                AvailableSeats = 20,
                Price = tour.BasePrice == 0 ? 25000.00m : tour.BasePrice
            });
        }
        await _context.SaveChangesAsync();
    }

    private async Task SeedItineraryAsync(Tour? multipathTour)
    {
        var tours = await _context.Tours.ToListAsync();
        foreach (var tour in tours)
        {
            if (await _context.Itineraries.AnyAsync(i => i.TourId == tour.TourId))
            {
                continue;
            }

            if (multipathTour is not null && tour.TourId == multipathTour.TourId)
            {
                AddDay(tour, 1, "Arrival in London", "Airport pickup and check-in, evening Thames river walk.");
                AddDay(tour, 2, "City Highlights", "Buckingham Palace, Big Ben, London Eye and Westminster Abbey.");
                AddDay(tour, 3, "Museums & Markets", "British Museum in the morning, Camden Market in the afternoon.");
                continue;
            }

            var days = Math.Min(tour.DurationDays == 0 ? 3 : tour.DurationDays, 5);
            for (var day = 1; day <= days; day++)
            {
                if (day == 1)
                {
                    AddDay(tour, day, "Arrival", $"Arrive and check in; orientation walk around {tour.Title}.");
                }
                else if (day == days)
                {
                    AddDay(tour, day, "Departure", "Free time for last-minute shopping, then transfer for departure.");
                }
                else
                {
                    AddDay(tour, day, "Sightseeing & Local Experiences", $"Guided sightseeing and local experiences, day {day} of the tour.");
                }
            }
        }
        await _context.SaveChangesAsync();
    }

    private void AddDay(Tour tour, int dayNumber, string title, string description) =>
        _context.Itineraries.Add(new Itinerary { TourId = tour.TourId, DayNumber = dayNumber, Title = title, Description = description });

    private async Task SeedAddonsAsync()
    {
        var tours = await _context.Tours.ToListAsync();
        foreach (var tour in tours)
        {
            if (await _context.TourAddons.AnyAsync(a => a.TourId == tour.TourId && a.Status))
            {
                continue;
            }
            _context.TourAddons.Add(new TourAddon
            {
                TourId = tour.TourId,
                AddonName = "Travel Insurance",
                Description = "Comprehensive travel insurance for the duration of the trip.",
                Price = 1500.00m,
                PriceType = PriceType.PER_PERSON,
                IsOptional = true,
                DisplayOrder = 1,
                Status = true
            });
        }
        await _context.SaveChangesAsync();
    }
}
