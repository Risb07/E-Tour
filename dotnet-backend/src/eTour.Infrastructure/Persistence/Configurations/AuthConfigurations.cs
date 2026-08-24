using eTour.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace eTour.Infrastructure.Persistence.Configurations;

public class RoleConfiguration : IEntityTypeConfiguration<Role>
{
    public void Configure(EntityTypeBuilder<Role> b)
    {
        b.ToTable("roles");
        b.HasKey(x => x.RoleId);
        b.Property(x => x.RoleId).HasColumnName("role_id");
        b.Property(x => x.RoleName).HasColumnName("role_name").IsRequired().HasMaxLength(50);
        b.Property(x => x.Description).HasColumnName("description");
        b.Property(x => x.Status).HasColumnName("status");
        b.HasIndex(x => x.RoleName).IsUnique();

        b.HasMany(x => x.Users).WithOne(x => x.Role).HasForeignKey(x => x.RoleId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class UserConfiguration : IEntityTypeConfiguration<User>
{
    public void Configure(EntityTypeBuilder<User> b)
    {
        b.ToTable("users");
        b.HasKey(x => x.UserId);
        b.Property(x => x.UserId).HasColumnName("user_id");
        b.Property(x => x.FirstName).HasColumnName("first_name").IsRequired().HasMaxLength(100);
        b.Property(x => x.LastName).HasColumnName("last_name").IsRequired().HasMaxLength(100);
        b.Property(x => x.Email).HasColumnName("email").IsRequired().HasMaxLength(150);
        b.Property(x => x.PasswordHash).HasColumnName("password_hash").IsRequired();
        b.Property(x => x.Phone).HasColumnName("phone").HasMaxLength(20);
        // No .HasDefaultValue(): same landmine as Passenger.NeedsExtraBed (see that config's
        // comment) - the real column has no genuine SQL DEFAULT, only a Java field initializer.
        // Harmless for a string today since app code always sets a non-null value, but kept
        // consistent so this can never silently start dropping the column from INSERTs.
        b.Property(x => x.PreferredLanguage).HasColumnName("preferred_language").HasMaxLength(10);
        b.Property(x => x.Status).HasColumnName("status");
        b.Property(x => x.RoleId).HasColumnName("role_id");
        // Federated sign-in columns - nullable in the shared schema, same as the Java entity.
        b.Property(x => x.AuthProvider).HasColumnName("auth_provider").HasMaxLength(20);
        b.Property(x => x.GoogleSub).HasColumnName("google_sub").HasMaxLength(100);
        b.Property(x => x.AvatarUrl).HasColumnName("avatar_url").HasMaxLength(500);
        b.HasIndex(x => x.Email).IsUnique();

        b.HasOne(x => x.Role).WithMany(x => x.Users).HasForeignKey(x => x.RoleId).OnDelete(DeleteBehavior.Restrict);
    }
}

public class CustomerConfiguration : IEntityTypeConfiguration<Customer>
{
    public void Configure(EntityTypeBuilder<Customer> b)
    {
        b.ToTable("customer");
        b.HasKey(x => x.CustomerId);
        b.Property(x => x.CustomerId).HasColumnName("customer_id");
        b.Property(x => x.FullName).HasColumnName("full_name").IsRequired().HasMaxLength(150);
        b.Property(x => x.Email).HasColumnName("email").IsRequired().HasMaxLength(150);
        b.Property(x => x.Phone).HasColumnName("phone").HasMaxLength(10);
        b.Property(x => x.UserId).HasColumnName("user_id");
        b.HasIndex(x => x.UserId).IsUnique();

        b.HasOne(x => x.User).WithOne(x => x.Customer).HasForeignKey<Customer>(x => x.UserId).OnDelete(DeleteBehavior.Cascade);
    }
}
