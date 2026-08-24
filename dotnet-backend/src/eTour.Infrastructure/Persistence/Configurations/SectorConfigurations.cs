using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace eTour.Infrastructure.Persistence.Configurations;

public class SectorConfiguration : IEntityTypeConfiguration<Sector>
{
    public void Configure(EntityTypeBuilder<Sector> b)
    {
        b.ToTable("sector");
        b.HasKey(x => x.SectorId);
        b.Property(x => x.SectorId).HasColumnName("sector_id");
        b.Property(x => x.Name).HasColumnName("name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.IconUrl).HasColumnName("icon_url");
        b.Property(x => x.ImageUrl).HasColumnName("image_url");
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.SortOrder).HasColumnName("sort_order");
        b.Property(x => x.TourCount).HasColumnName("tour_count");

        b.HasMany(x => x.SubSectors).WithOne(x => x.Sector).HasForeignKey(x => x.SectorId).OnDelete(DeleteBehavior.Cascade);
    }
}

public class SubSectorConfiguration : IEntityTypeConfiguration<SubSector>
{
    public void Configure(EntityTypeBuilder<SubSector> b)
    {
        b.ToTable("sub_sector");
        b.HasKey(x => x.SubSectorId);
        b.Property(x => x.SubSectorId).HasColumnName("sub_sector_id");
        b.Property(x => x.Name).HasColumnName("name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.IconUrl).HasColumnName("icon_url");
        b.Property(x => x.ImageUrl).HasColumnName("image_url");
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.SortOrder).HasColumnName("sort_order");
        b.Property(x => x.SectorId).HasColumnName("sector_id");

        b.HasMany(x => x.Products).WithOne(x => x.SubSector).HasForeignKey(x => x.SubSectorId).OnDelete(DeleteBehavior.Cascade);
    }
}

public class TourProductConfiguration : IEntityTypeConfiguration<TourProduct>
{
    public void Configure(EntityTypeBuilder<TourProduct> b)
    {
        b.ToTable("tour_product");
        b.HasKey(x => x.ProductId);
        b.Property(x => x.ProductId).HasColumnName("product_id");
        b.Property(x => x.Name).HasColumnName("name").IsRequired().HasMaxLength(200);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.ImageUrl).HasColumnName("image_url");
        b.Property(x => x.BaseCost).HasColumnName("base_cost").HasPrecision(10, 2);
        b.Property(x => x.DurationDays).HasColumnName("duration_days");
        b.Property(x => x.DurationNights).HasColumnName("duration_nights");
        b.Property(x => x.TourCode).HasColumnName("tour_code").HasMaxLength(50);
        b.Property(x => x.StartDate).HasColumnName("start_date");
        b.Property(x => x.EndDate).HasColumnName("end_date");
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.SortOrder).HasColumnName("sort_order");
        b.Property(x => x.SubSectorId).HasColumnName("sub_sector_id");
        b.Property(x => x.TourId).HasColumnName("tour_id");

        b.HasOne(x => x.Tour).WithMany().HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.SetNull);
    }
}

public class TourTagRuleConfiguration : IEntityTypeConfiguration<TourTagRule>
{
    public void Configure(EntityTypeBuilder<TourTagRule> b)
    {
        b.ToTable("tour_tag_rule");
        b.HasKey(x => x.RuleId);
        b.Property(x => x.RuleId).HasColumnName("rule_id");
        b.Property(x => x.Name).HasColumnName("name").IsRequired().HasMaxLength(150);
        b.Property(x => x.MatchField).HasColumnName("match_field").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.MatchOperator).HasColumnName("match_operator").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.MatchValue).HasColumnName("match_value").HasMaxLength(200);
        b.Property(x => x.MatchValueTo).HasColumnName("match_value_to").HasMaxLength(200);
        b.Property(x => x.Priority).HasColumnName("priority");
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.TargetCategoryId).HasColumnName("target_category_id");
        b.Property(x => x.TargetSubSectorId).HasColumnName("target_sub_sector_id");

        b.HasOne(x => x.TargetCategory).WithMany().HasForeignKey(x => x.TargetCategoryId).OnDelete(DeleteBehavior.Restrict);
        b.HasOne(x => x.TargetSubSector).WithMany().HasForeignKey(x => x.TargetSubSectorId).OnDelete(DeleteBehavior.Restrict);
    }
}
