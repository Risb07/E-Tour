using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace eTour.Infrastructure.Persistence.Configurations;

public class TourConfiguration : IEntityTypeConfiguration<Tour>
{
    public void Configure(EntityTypeBuilder<Tour> b)
    {
        b.ToTable("tour");
        b.HasKey(x => x.TourId);
        b.Property(x => x.TourId).HasColumnName("tour_id");
        b.Property(x => x.Title).HasColumnName("title").IsRequired().HasMaxLength(200);
        b.Property(x => x.Description).HasColumnName("description").HasColumnType("TEXT");
        b.Property(x => x.DurationDays).HasColumnName("duration_days");
        b.Property(x => x.BasePrice).HasColumnName("base_price").HasPrecision(10, 2);
        b.Property(x => x.TourCode).HasColumnName("tour_code").HasConversion<string>().HasMaxLength(10);
        b.Property(x => x.Status).HasColumnName("status").HasConversion<string>().HasMaxLength(20);

        b.HasMany(x => x.TourCosts).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Schedules).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Itineraries).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Addons).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Media).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Contents).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.JourneyDetails).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.StayMeals).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.RoomCharges).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Reviews).WithOne(x => x.Tour).HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
    }
}

public class TourCostConfiguration : IEntityTypeConfiguration<TourCost>
{
    public void Configure(EntityTypeBuilder<TourCost> b)
    {
        // Lower case, even though the Java entity says @Table(name = "TOURCOST").
        // Spring Boot's physical naming strategy lower-cases identifiers before
        // they reach the database, so Hibernate created - and queries - `tourcost`.
        // EF Core takes the name verbatim, and MySQL on Linux is case-sensitive,
        // so the upper-case form here raised "Table 'etour.TOURCOST' doesn't
        // exist" and broke booking creation under this backend entirely. Every
        // other table in this file already matches the database exactly; this was
        // the only one that did not.
        b.ToTable("tourcost");
        b.HasKey(x => x.CostId);
        b.Property(x => x.CostId).HasColumnName("cost_id");
        b.Property(x => x.BasePrice).HasColumnName("base_price").HasPrecision(10, 2);
        b.Property(x => x.SinglePersonCost).HasColumnName("single_person_cost").HasPrecision(10, 2);
        b.Property(x => x.ExtraPersonCost).HasColumnName("extra_person_cost").HasPrecision(10, 2);
        b.Property(x => x.ChildWithBedCost).HasColumnName("child_with_bed_cost").HasPrecision(10, 2);
        b.Property(x => x.ChildWithoutBedCost).HasColumnName("child_without_bed_cost").HasPrecision(10, 2);
        b.Property(x => x.ValidFrom).HasColumnName("valid_from");
        b.Property(x => x.ValidTo).HasColumnName("valid_to");
        b.Property(x => x.Status).HasColumnName("status");
        b.Property(x => x.TourId).HasColumnName("tour_id");
    }
}

public class TourScheduleConfiguration : IEntityTypeConfiguration<TourSchedule>
{
    public void Configure(EntityTypeBuilder<TourSchedule> b)
    {
        b.ToTable("tour_schedule");
        b.HasKey(x => x.ScheduleId);
        b.Property(x => x.ScheduleId).HasColumnName("schedule_id");
        b.Property(x => x.DepartureDate).HasColumnName("departure_date");
        b.Property(x => x.ReturnDate).HasColumnName("return_date");
        b.Property(x => x.AvailableSeats).HasColumnName("available_seats");
        b.Property(x => x.Price).HasColumnName("price").HasPrecision(10, 2);
        b.Property(x => x.TourId).HasColumnName("tour_id");

        b.HasMany(x => x.Bookings).WithOne(x => x.Schedule).HasForeignKey(x => x.ScheduleId).OnDelete(DeleteBehavior.Restrict);
        b.HasMany(x => x.Carts).WithOne(x => x.Schedule).HasForeignKey(x => x.ScheduleId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class TourAddonConfiguration : IEntityTypeConfiguration<TourAddon>
{
    public void Configure(EntityTypeBuilder<TourAddon> b)
    {
        b.ToTable("tour_addon");
        b.HasKey(x => x.AddonId);
        b.Property(x => x.AddonId).HasColumnName("addon_id");
        b.Property(x => x.AddonName).HasColumnName("addon_name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.Price).HasColumnName("price").HasPrecision(10, 2);
        b.Property(x => x.IsOptional).HasColumnName("is_optional");
        b.Property(x => x.DisplayOrder).HasColumnName("display_order");
        b.Property(x => x.Status).HasColumnName("status");
        b.Property(x => x.PriceType).HasColumnName("price_type").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.TourId).HasColumnName("tour_id");
    }
}

public class TourContentConfiguration : IEntityTypeConfiguration<TourContent>
{
    public void Configure(EntityTypeBuilder<TourContent> b)
    {
        b.ToTable("tour_content");
        b.HasKey(x => x.TourContentId);
        b.Property(x => x.TourContentId).HasColumnName("tour_content_id");
        // No .HasDefaultValue(): see Passenger.NeedsExtraBed's config comment for why.
        b.Property(x => x.LanguageCode).HasColumnName("language_code").HasMaxLength(10);
        b.Property(x => x.ContentText).HasColumnName("content_text").HasColumnType("TEXT");
        b.Property(x => x.Status).HasColumnName("status");
        b.Property(x => x.ContentType).HasColumnName("content_type").HasConversion<string>().HasMaxLength(30);
        b.Property(x => x.TourId).HasColumnName("tour_id");
    }
}

public class TourMediaConfiguration : IEntityTypeConfiguration<TourMedia>
{
    public void Configure(EntityTypeBuilder<TourMedia> b)
    {
        b.ToTable("tour_media");
        b.HasKey(x => x.MediaId);
        b.Property(x => x.MediaId).HasColumnName("media_id");
        b.Property(x => x.FilePath).HasColumnName("file_path").IsRequired();
        b.Property(x => x.MimeType).HasColumnName("mime_type").HasMaxLength(100);
        b.Property(x => x.DisplayOrder).HasColumnName("display_order");
        b.Property(x => x.Status).HasColumnName("status");
        b.Property(x => x.MediaType).HasColumnName("media_type").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.TabContext).HasColumnName("tab_context").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.TourId).HasColumnName("tour_id");
    }
}

public class ItineraryConfiguration : IEntityTypeConfiguration<Itinerary>
{
    public void Configure(EntityTypeBuilder<Itinerary> b)
    {
        b.ToTable("itinerary");
        b.HasKey(x => x.ItineraryId);
        b.Property(x => x.ItineraryId).HasColumnName("itinerary_id");
        b.Property(x => x.DayNumber).HasColumnName("day_number");
        b.Property(x => x.Title).HasColumnName("title").IsRequired().HasMaxLength(200);
        b.Property(x => x.Description).HasColumnName("description").HasColumnType("TEXT");
        b.Property(x => x.TourId).HasColumnName("tour_id");
    }
}

public class JourneyDetailConfiguration : IEntityTypeConfiguration<JourneyDetail>
{
    public void Configure(EntityTypeBuilder<JourneyDetail> b)
    {
        b.ToTable("journey_detail");
        b.HasKey(x => x.JourneyId);
        b.Property(x => x.JourneyId).HasColumnName("journey_id");
        b.Property(x => x.SequenceNo).HasColumnName("sequence_no");
        b.Property(x => x.Notes).HasColumnName("notes");
        b.Property(x => x.ModeOfTravel).HasColumnName("mode_of_travel").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.TourId).HasColumnName("tour_id");
        b.Property(x => x.FromLocationId).HasColumnName("from_location_id");
        b.Property(x => x.ToLocationId).HasColumnName("to_location_id");

        b.HasOne(x => x.FromLocation).WithMany().HasForeignKey(x => x.FromLocationId).OnDelete(DeleteBehavior.Restrict);
        b.HasOne(x => x.ToLocation).WithMany().HasForeignKey(x => x.ToLocationId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class StayMealConfiguration : IEntityTypeConfiguration<StayMeal>
{
    public void Configure(EntityTypeBuilder<StayMeal> b)
    {
        b.ToTable("stay_meal");
        b.HasKey(x => x.StayMealId);
        b.Property(x => x.StayMealId).HasColumnName("stay_meal_id");
        b.Property(x => x.DayNumber).HasColumnName("day_number");
        b.Property(x => x.HotelName).HasColumnName("hotel_name").HasMaxLength(150);
        b.Property(x => x.Breakfast).HasColumnName("breakfast");
        b.Property(x => x.Lunch).HasColumnName("lunch");
        b.Property(x => x.Dinner).HasColumnName("dinner");
        b.Property(x => x.TourId).HasColumnName("tour_id");
        b.Property(x => x.LocationId).HasColumnName("location_id");

        b.HasOne(x => x.LocationEntity).WithMany().HasForeignKey(x => x.LocationId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class RoomChargeConfiguration : IEntityTypeConfiguration<RoomCharge>
{
    public void Configure(EntityTypeBuilder<RoomCharge> b)
    {
        b.ToTable("room_charge");
        b.HasKey(x => x.RoomChargeId);
        b.Property(x => x.RoomChargeId).HasColumnName("room_charge_id");
        b.Property(x => x.Charge).HasColumnName("charge").HasPrecision(10, 2);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.Occupancy).HasColumnName("occupancy").HasConversion<string>().HasMaxLength(30);
        b.Property(x => x.TourId).HasColumnName("tour_id");

        b.HasIndex(x => new { x.TourId, x.Occupancy }).IsUnique();
    }
}
