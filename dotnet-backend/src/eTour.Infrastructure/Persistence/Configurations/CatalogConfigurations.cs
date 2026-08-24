using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace eTour.Infrastructure.Persistence.Configurations;

public class AdBannerConfiguration : IEntityTypeConfiguration<AdBanner>
{
    public void Configure(EntityTypeBuilder<AdBanner> b)
    {
        b.ToTable("ad_banner");
        b.HasKey(x => x.AdId);
        b.Property(x => x.AdId).HasColumnName("ad_id");
        b.Property(x => x.Title).HasColumnName("title").IsRequired().HasMaxLength(200);
        b.Property(x => x.ImageUrl).HasColumnName("image_url");
        b.Property(x => x.LinkUrl).HasColumnName("link_url");
        b.Property(x => x.Position).HasColumnName("position").HasMaxLength(50);
        b.Property(x => x.Status).HasColumnName("status");
    }
}

public class CategoryConfiguration : IEntityTypeConfiguration<Category>
{
    public void Configure(EntityTypeBuilder<Category> b)
    {
        b.ToTable("category");
        b.HasKey(x => x.CategoryId);
        b.Property(x => x.CategoryId).HasColumnName("category_id");
        b.Property(x => x.CategoryName).HasColumnName("category_name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.ImageUrl).HasColumnName("image_url");
        b.Property(x => x.Status).HasColumnName("status");
        b.Property(x => x.CategoryCode).HasColumnName("category_code").HasMaxLength(10);
        b.Property(x => x.IsFeatured).HasColumnName("is_featured").HasMaxLength(1);
        b.Property(x => x.ParentCategoryId).HasColumnName("parent_category_id");

        b.HasOne(x => x.ParentCategory).WithMany(x => x.ChildCategories)
            .HasForeignKey(x => x.ParentCategoryId).OnDelete(DeleteBehavior.Restrict);

        // Explicit join column names are required here - without them EF Core invents its own
        // shadow property names (e.g. "CategoriesCategoryId") instead of the "tour_id"/
        // "category_id" columns Hibernate's @JoinTable already created, and every query against
        // this relationship fails at runtime with "Unknown column" (only surfaces against a real
        // MySQL database - the InMemory test provider doesn't validate column names).
        b.HasMany(x => x.Tours).WithMany(x => x.Categories)
            .UsingEntity<Dictionary<string, object>>(
                "tour_category",
                j => j.HasOne<Tour>().WithMany().HasForeignKey("tour_id"),
                j => j.HasOne<Category>().WithMany().HasForeignKey("category_id"));
    }
}

public class ContactEnquiryConfiguration : IEntityTypeConfiguration<ContactEnquiry>
{
    public void Configure(EntityTypeBuilder<ContactEnquiry> b)
    {
        b.ToTable("contact_enquiry");
        b.HasKey(x => x.EnquiryId);
        b.Property(x => x.EnquiryId).HasColumnName("enquiry_id");
        b.Property(x => x.Name).HasColumnName("name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Email).HasColumnName("email").IsRequired().HasMaxLength(150);
        b.Property(x => x.Phone).HasColumnName("phone").HasMaxLength(20);
        b.Property(x => x.Subject).HasColumnName("subject").HasMaxLength(200);
        b.Property(x => x.Message).HasColumnName("message").IsRequired();
        b.Property(x => x.Status).HasColumnName("status").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.CreatedAt).HasColumnName("created_at");
        b.Property(x => x.UpdatedAt).HasColumnName("updated_at");
    }
}

public class ContentConfiguration : IEntityTypeConfiguration<Content>
{
    public void Configure(EntityTypeBuilder<Content> b)
    {
        b.ToTable("content");
        b.HasKey(x => x.ContentId);
        b.Property(x => x.ContentId).HasColumnName("content_id");
        b.Property(x => x.ContentKey).HasColumnName("content_key").IsRequired().HasMaxLength(100);
        b.Property(x => x.PageName).HasColumnName("page_name").HasMaxLength(100);
        // No .HasDefaultValue(): see Passenger.NeedsExtraBed's config comment for why.
        b.Property(x => x.LanguageCode).HasColumnName("language_code").HasMaxLength(10);
        b.Property(x => x.ContentValue).HasColumnName("content_value");
        b.Property(x => x.MediaUrl).HasColumnName("media_url");
        b.Property(x => x.LinkUrl).HasColumnName("link_url");
        b.Property(x => x.DisplayOrder).HasColumnName("display_order");
        b.Property(x => x.Status).HasColumnName("status");
    }
}

public class CrawlingTextConfiguration : IEntityTypeConfiguration<CrawlingText>
{
    public void Configure(EntityTypeBuilder<CrawlingText> b)
    {
        b.ToTable("crawling_text");
        b.HasKey(x => x.CrawlingTextId);
        b.Property(x => x.CrawlingTextId).HasColumnName("crawling_text_id");
        b.Property(x => x.Text).HasColumnName("text").IsRequired();
        b.Property(x => x.SortOrder).HasColumnName("sort_order");
        b.Property(x => x.IsActive).HasColumnName("is_active");
    }
}

public class LocationConfiguration : IEntityTypeConfiguration<Location>
{
    public void Configure(EntityTypeBuilder<Location> b)
    {
        b.ToTable("location");
        b.HasKey(x => x.LocationId);
        b.Property(x => x.LocationId).HasColumnName("location_id");
        b.Property(x => x.LocationName).HasColumnName("location_name").IsRequired().HasMaxLength(150);
        b.Property(x => x.StateProvince).HasColumnName("state_province").HasMaxLength(100);
        b.Property(x => x.Country).HasColumnName("country").HasMaxLength(100);
        b.Property(x => x.CountryCode).HasColumnName("country_code").HasMaxLength(2);
        b.Property(x => x.Status).HasColumnName("status");
    }
}

public class NavMenuItemConfiguration : IEntityTypeConfiguration<NavMenuItem>
{
    public void Configure(EntityTypeBuilder<NavMenuItem> b)
    {
        b.ToTable("nav_menu_item");
        b.HasKey(x => x.NavMenuItemId);
        b.Property(x => x.NavMenuItemId).HasColumnName("nav_menu_item_id");
        b.Property(x => x.Label).HasColumnName("label").IsRequired().HasMaxLength(100);
        b.Property(x => x.Link).HasColumnName("link").HasMaxLength(255);
        b.Property(x => x.SortOrder).HasColumnName("sort_order");
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.ParentItemId).HasColumnName("parent_item_id");

        b.HasOne(x => x.ParentItem).WithMany(x => x.Children)
            .HasForeignKey(x => x.ParentItemId).OnDelete(DeleteBehavior.Restrict);
    }
}
