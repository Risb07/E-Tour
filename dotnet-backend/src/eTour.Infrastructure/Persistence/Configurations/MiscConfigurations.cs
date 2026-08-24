using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace eTour.Infrastructure.Persistence.Configurations;

public class ReviewConfiguration : IEntityTypeConfiguration<Review>
{
    public void Configure(EntityTypeBuilder<Review> b)
    {
        b.ToTable("review");
        b.HasKey(x => x.ReviewId);
        b.Property(x => x.ReviewId).HasColumnName("review_id");
        b.Property(x => x.Rating).HasColumnName("rating");
        b.Property(x => x.Comment).HasColumnName("comment");
        b.Property(x => x.TourId).HasColumnName("tour_id");
        b.Property(x => x.CustomerId).HasColumnName("customer_id");

        b.HasOne(x => x.Customer).WithMany().HasForeignKey(x => x.CustomerId).OnDelete(DeleteBehavior.Cascade);
    }
}

public class WishlistItemConfiguration : IEntityTypeConfiguration<WishlistItem>
{
    public void Configure(EntityTypeBuilder<WishlistItem> b)
    {
        b.ToTable("wishlist_item");
        b.HasKey(x => x.WishlistItemId);
        b.Property(x => x.WishlistItemId).HasColumnName("wishlist_item_id");
        b.Property(x => x.CreatedAt).HasColumnName("created_at");
        b.Property(x => x.CustomerId).HasColumnName("customer_id");
        b.Property(x => x.TourId).HasColumnName("tour_id");

        b.HasOne(x => x.Customer).WithMany().HasForeignKey(x => x.CustomerId).OnDelete(DeleteBehavior.Cascade);
        b.HasOne(x => x.Tour).WithMany().HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        b.HasIndex(x => new { x.CustomerId, x.TourId }).IsUnique();
    }
}

public class NewsletterSubscriberConfiguration : IEntityTypeConfiguration<NewsletterSubscriber>
{
    public void Configure(EntityTypeBuilder<NewsletterSubscriber> b)
    {
        b.ToTable("newsletter_subscriber");
        b.HasKey(x => x.SubscriberId);
        b.Property(x => x.SubscriberId).HasColumnName("subscriber_id");
        b.Property(x => x.Email).HasColumnName("email").IsRequired().HasMaxLength(190);
        b.Property(x => x.Name).HasColumnName("name").HasMaxLength(150);
        b.Property(x => x.Active).HasColumnName("active");
        b.Property(x => x.SubscribedAt).HasColumnName("subscribed_at");
        b.Property(x => x.UnsubscribedAt).HasColumnName("unsubscribed_at");
        b.HasIndex(x => x.Email).IsUnique();
    }
}

public class ExcelUploadBatchConfiguration : IEntityTypeConfiguration<ExcelUploadBatch>
{
    public void Configure(EntityTypeBuilder<ExcelUploadBatch> b)
    {
        b.ToTable("excel_upload_batch");
        b.HasKey(x => x.BatchId);
        b.Property(x => x.BatchId).HasColumnName("batch_id");
        b.Property(x => x.FileName).HasColumnName("file_name").IsRequired().HasMaxLength(255);
        b.Property(x => x.FilePath).HasColumnName("file_path");
        b.Property(x => x.TotalRows).HasColumnName("total_rows");
        b.Property(x => x.SuccessRows).HasColumnName("success_rows");
        b.Property(x => x.FailedRows).HasColumnName("failed_rows");
        b.Property(x => x.Status).HasColumnName("status").HasConversion<string>().HasMaxLength(30);
        b.Property(x => x.UploadedAt).HasColumnName("uploaded_at");
        b.Property(x => x.UploadedById).HasColumnName("uploaded_by");

        b.HasOne(x => x.UploadedBy).WithMany().HasForeignKey(x => x.UploadedById).OnDelete(DeleteBehavior.Restrict);
    }
}
