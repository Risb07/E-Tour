using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace eTour.Infrastructure.Persistence;

public class EtourDbContext : DbContext
{
    public EtourDbContext(DbContextOptions<EtourDbContext> options) : base(options)
    {
    }

    public DbSet<AdBanner> AdBanners => Set<AdBanner>();
    public DbSet<Role> Roles => Set<Role>();
    public DbSet<User> Users => Set<User>();
    public DbSet<Customer> Customers => Set<Customer>();
    public DbSet<Category> Categories => Set<Category>();
    public DbSet<ContactEnquiry> ContactEnquiries => Set<ContactEnquiry>();
    public DbSet<Content> Contents => Set<Content>();
    public DbSet<CrawlingText> CrawlingTexts => Set<CrawlingText>();
    public DbSet<Location> Locations => Set<Location>();
    public DbSet<NavMenuItem> NavMenuItems => Set<NavMenuItem>();
    public DbSet<Tour> Tours => Set<Tour>();
    public DbSet<TourCost> TourCosts => Set<TourCost>();
    public DbSet<TourSchedule> TourSchedules => Set<TourSchedule>();
    public DbSet<TourAddon> TourAddons => Set<TourAddon>();
    public DbSet<TourContent> TourContents => Set<TourContent>();
    public DbSet<TourMedia> TourMedia => Set<TourMedia>();
    public DbSet<Itinerary> Itineraries => Set<Itinerary>();
    public DbSet<JourneyDetail> JourneyDetails => Set<JourneyDetail>();
    public DbSet<StayMeal> StayMeals => Set<StayMeal>();
    public DbSet<RoomCharge> RoomCharges => Set<RoomCharge>();
    public DbSet<Sector> Sectors => Set<Sector>();
    public DbSet<SubSector> SubSectors => Set<SubSector>();
    public DbSet<TourProduct> TourProducts => Set<TourProduct>();
    public DbSet<TourTagRule> TourTagRules => Set<TourTagRule>();
    public DbSet<Review> Reviews => Set<Review>();
    public DbSet<WishlistItem> WishlistItems => Set<WishlistItem>();
    public DbSet<NewsletterSubscriber> NewsletterSubscribers => Set<NewsletterSubscriber>();
    public DbSet<ExcelUploadBatch> ExcelUploadBatches => Set<ExcelUploadBatch>();
    public DbSet<Booking> Bookings => Set<Booking>();
    public DbSet<BookingAddon> BookingAddons => Set<BookingAddon>();
    public DbSet<Cart> Carts => Set<Cart>();
    public DbSet<CartAddon> CartAddons => Set<CartAddon>();
    public DbSet<Passenger> Passengers => Set<Passenger>();
    public DbSet<Payment> Payments => Set<Payment>();
    public DbSet<Invoice> Invoices => Set<Invoice>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(EtourDbContext).Assembly);
        base.OnModelCreating(modelBuilder);
    }
}
