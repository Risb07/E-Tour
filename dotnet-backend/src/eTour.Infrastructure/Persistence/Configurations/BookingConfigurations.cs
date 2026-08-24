using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace eTour.Infrastructure.Persistence.Configurations;

public class BookingConfiguration : IEntityTypeConfiguration<Booking>
{
    public void Configure(EntityTypeBuilder<Booking> b)
    {
        b.ToTable("booking");
        b.HasKey(x => x.BookingId);
        b.Property(x => x.BookingId).HasColumnName("booking_id");
        b.Property(x => x.BookingDate).HasColumnName("booking_date");
        b.Property(x => x.TotalAmount).HasColumnName("total_amount").HasPrecision(10, 2);
        b.Property(x => x.OrderNumber).HasColumnName("order_number").HasMaxLength(50);
        b.Property(x => x.NumberOfPassengers).HasColumnName("number_of_passengers");
        b.Property(x => x.AdultCount).HasColumnName("adult_count");
        b.Property(x => x.ChildCount).HasColumnName("child_count");
        b.Property(x => x.BookingStatus).HasColumnName("booking_status").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.CustomerId).HasColumnName("customer_id");
        b.Property(x => x.ScheduleId).HasColumnName("schedule_id");

        b.HasOne(x => x.Customer).WithMany().HasForeignKey(x => x.CustomerId).OnDelete(DeleteBehavior.Restrict);
        b.HasMany(x => x.Addons).WithOne(x => x.Booking).HasForeignKey(x => x.BookingId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Passengers).WithOne(x => x.Booking).HasForeignKey(x => x.BookingId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(x => x.Payments).WithOne(x => x.Booking).HasForeignKey(x => x.BookingId).OnDelete(DeleteBehavior.Cascade);
    }
}

public class BookingAddonConfiguration : IEntityTypeConfiguration<BookingAddon>
{
    public void Configure(EntityTypeBuilder<BookingAddon> b)
    {
        b.ToTable("booking_addon");
        b.HasKey(x => x.BookingAddonId);
        b.Property(x => x.BookingAddonId).HasColumnName("booking_addon_id");
        b.Property(x => x.AddonName).HasColumnName("addon_name").IsRequired().HasMaxLength(150);
        b.Property(x => x.UnitPrice).HasColumnName("unit_price").HasPrecision(10, 2);
        b.Property(x => x.Quantity).HasColumnName("quantity");
        b.Property(x => x.TotalAddonCost).HasColumnName("total_addon_cost").HasPrecision(10, 2);
        b.Property(x => x.PriceType).HasColumnName("price_type").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.BookingId).HasColumnName("booking_id");
        b.Property(x => x.AddonId).HasColumnName("addon_id");

        b.HasOne(x => x.Addon).WithMany().HasForeignKey(x => x.AddonId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class CartConfiguration : IEntityTypeConfiguration<Cart>
{
    public void Configure(EntityTypeBuilder<Cart> b)
    {
        b.ToTable("cart");
        b.HasKey(x => x.CartId);
        b.Property(x => x.CartId).HasColumnName("cart_id");
        b.Property(x => x.PaxSummary).HasColumnName("pax_summary").HasMaxLength(100);
        b.Property(x => x.AdultCount).HasColumnName("adult_count");
        b.Property(x => x.ChildCount).HasColumnName("child_count");
        b.Property(x => x.EstimatedAmount).HasColumnName("estimated_amount").HasPrecision(10, 2);
        b.Property(x => x.CreatedAt).HasColumnName("created_at");
        b.Property(x => x.UpdatedAt).HasColumnName("updated_at");
        b.Property(x => x.Status).HasColumnName("status").HasConversion<string>().HasMaxLength(30);
        b.Property(x => x.CustomerId).HasColumnName("customer_id");
        b.Property(x => x.ScheduleId).HasColumnName("schedule_id");

        b.HasOne(x => x.Customer).WithMany().HasForeignKey(x => x.CustomerId).OnDelete(DeleteBehavior.Restrict);
        b.HasMany(x => x.Addons).WithOne(x => x.Cart).HasForeignKey(x => x.CartId).OnDelete(DeleteBehavior.Cascade);
    }
}

public class CartAddonConfiguration : IEntityTypeConfiguration<CartAddon>
{
    public void Configure(EntityTypeBuilder<CartAddon> b)
    {
        b.ToTable("cart_addon");
        b.HasKey(x => x.CartAddonId);
        b.Property(x => x.CartAddonId).HasColumnName("cart_addon_id");
        b.Property(x => x.Quantity).HasColumnName("quantity");
        b.Property(x => x.EstimatedCost).HasColumnName("estimated_cost").HasPrecision(10, 2);
        b.Property(x => x.CartId).HasColumnName("cart_id");
        b.Property(x => x.AddonId).HasColumnName("addon_id");

        b.HasOne(x => x.Addon).WithMany().HasForeignKey(x => x.AddonId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class PassengerConfiguration : IEntityTypeConfiguration<Passenger>
{
    public void Configure(EntityTypeBuilder<Passenger> b)
    {
        b.ToTable("passenger");
        b.HasKey(x => x.PassengerId);
        b.Property(x => x.PassengerId).HasColumnName("passenger_id");
        b.Property(x => x.FullName).HasColumnName("full_name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Gender).HasColumnName("gender").HasMaxLength(1);
        b.Property(x => x.Dob).HasColumnName("dob");
        b.Property(x => x.Nationality).HasColumnName("nationality").HasMaxLength(2);
        b.Property(x => x.IdProofType).HasColumnName("id_proof_type").HasMaxLength(30);
        b.Property(x => x.IdProofNumber).HasColumnName("id_proof_number").HasMaxLength(50);
        // No .HasDefaultValue() here on purpose: the real (Hibernate-created) column has no
        // actual SQL DEFAULT, just a Java field initializer - which is not a database default.
        // HasDefaultValue() marks the property ValueGeneratedOnAdd, and EF Core then OMITS it
        // from the INSERT whenever the value equals the CLR default for bool (false), trusting
        // the DB to fill it in. With no real DB default, MySQL writes NULL into this NOT-null-
        // enforced-only-by-C# column instead - and EF's post-insert read-back then throws
        // InvalidCastException converting that NULL back to bool. Application code always sets
        // this explicitly (BookingService.BuildPassengers), so no default is needed here.
        b.Property(x => x.NeedsExtraBed).HasColumnName("needs_extra_bed");
        b.Property(x => x.PassengerType).HasColumnName("passenger_type").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.Occupancy).HasColumnName("occupancy").HasConversion<string>().HasMaxLength(30);
        b.Property(x => x.RoomCharge).HasColumnName("room_charge").HasPrecision(10, 2);
        b.Property(x => x.PassengerPrice).HasColumnName("passenger_price").HasPrecision(10, 2);
        b.Property(x => x.AddressLine1).HasColumnName("address_line1").HasMaxLength(200);
        b.Property(x => x.AddressLine2).HasColumnName("address_line2").HasMaxLength(200);
        b.Property(x => x.City).HasColumnName("city").HasMaxLength(100);
        b.Property(x => x.State).HasColumnName("state").HasMaxLength(100);
        b.Property(x => x.Country).HasColumnName("country").HasMaxLength(100);
        b.Property(x => x.Pincode).HasColumnName("pincode").HasMaxLength(20);
        b.Property(x => x.BookingId).HasColumnName("booking_id");
        b.HasIndex(x => x.IdProofNumber).IsUnique();
    }
}

public class PaymentConfiguration : IEntityTypeConfiguration<Payment>
{
    public void Configure(EntityTypeBuilder<Payment> b)
    {
        b.ToTable("payment");
        b.HasKey(x => x.PaymentId);
        b.Property(x => x.PaymentId).HasColumnName("payment_id");
        b.Property(x => x.Amount).HasColumnName("amount").HasPrecision(10, 2);
        b.Property(x => x.PaymentMethod).HasColumnName("payment_method").HasMaxLength(30);
        b.Property(x => x.PaymentStatus).HasColumnName("payment_status").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.TransactionRef).HasColumnName("transaction_ref").HasMaxLength(100);
        b.Property(x => x.Gateway).HasColumnName("gateway").HasMaxLength(50);
        b.Property(x => x.CardSummary).HasColumnName("card_summary").HasMaxLength(50);
        b.Property(x => x.CreatedAt).HasColumnName("created_at");
        b.Property(x => x.UpdatedAt).HasColumnName("updated_at");
        b.Property(x => x.BookingId).HasColumnName("booking_id");
    }
}

public class InvoiceConfiguration : IEntityTypeConfiguration<Invoice>
{
    public void Configure(EntityTypeBuilder<Invoice> b)
    {
        b.ToTable("invoice");
        b.HasKey(x => x.InvoiceId);
        b.Property(x => x.InvoiceId).HasColumnName("invoice_id");
        b.Property(x => x.InvoiceNumber).HasColumnName("invoice_number").IsRequired().HasMaxLength(50);
        b.Property(x => x.InvoiceDate).HasColumnName("invoice_date");
        b.Property(x => x.SubTotal).HasColumnName("sub_total").HasPrecision(10, 2);
        b.Property(x => x.TaxAmount).HasColumnName("tax_amount").HasPrecision(10, 2);
        b.Property(x => x.DiscountAmount).HasColumnName("discount_amount").HasPrecision(10, 2);
        b.Property(x => x.TotalAmount).HasColumnName("total_amount").HasPrecision(10, 2);
        b.Property(x => x.PdfUrl).HasColumnName("pdf_url");
        b.Property(x => x.InvoiceStatus).HasColumnName("invoice_status").HasConversion<string>().HasMaxLength(20);
        b.Property(x => x.CreatedAt).HasColumnName("created_at");
        b.Property(x => x.BookingId).HasColumnName("booking_id");
        b.Property(x => x.PaymentId).HasColumnName("payment_id");
        b.Property(x => x.CustomerId).HasColumnName("customer_id");
        b.HasIndex(x => x.InvoiceNumber).IsUnique();

        b.HasOne(x => x.Booking).WithMany().HasForeignKey(x => x.BookingId).OnDelete(DeleteBehavior.Restrict);
        b.HasOne(x => x.Payment).WithMany().HasForeignKey(x => x.PaymentId).OnDelete(DeleteBehavior.Restrict);
        b.HasOne(x => x.Customer).WithMany().HasForeignKey(x => x.CustomerId).OnDelete(DeleteBehavior.Restrict);
    }
}
