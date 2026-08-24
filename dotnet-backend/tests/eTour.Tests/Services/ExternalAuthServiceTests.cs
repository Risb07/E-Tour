using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging.Abstractions;
using NUnit.Framework;

namespace eTour.Tests.Services;

/// <summary>
/// Account-linking rules for OAuth sign-in, exercised without any HTTP or a live provider - the
/// controller's only job is to turn provider claims into an ExternalIdentity, so everything that
/// can actually go wrong (linking the wrong account, provisioning duplicates, letting a disabled
/// user back in, granting the wrong role) is decided here and pinned here.
/// </summary>
[TestFixture]
public class ExternalAuthServiceTests
{
    private EtourDbContext _context = null!;
    private ExternalAuthService _service = null!;
    private long _customerRoleId;

    [SetUp]
    public async Task SetUp()
    {
        _context = TestDbContextFactory.Create();

        var customerRole = new Role { RoleName = "CUSTOMER", Status = true };
        var adminRole = new Role { RoleName = "ADMIN", Status = true };
        _context.Roles.AddRange(customerRole, adminRole);
        await _context.SaveChangesAsync();
        _customerRoleId = customerRole.RoleId;

        _context.Users.AddRange(
            new User
            {
                FirstName = "Ada", LastName = "Lovelace", Email = "Ada@Example.com",
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("irrelevant"),
                Status = true, RoleId = adminRole.RoleId
            },
            new User
            {
                FirstName = "Dis", LastName = "Abled", Email = "disabled@example.com",
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("irrelevant"),
                Status = false, RoleId = customerRole.RoleId
            });
        await _context.SaveChangesAsync();

        _service = new ExternalAuthService(
            new GenericRepository<User, long>(_context),
            new GenericRepository<Role, long>(_context),
            new GenericRepository<Customer, long>(_context),
            new FakeJwtTokenService(),
            NullLogger<ExternalAuthService>.Instance);
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    private static ExternalIdentity Identity(string email, string? first = "Grace", string? last = "Hopper",
        string subject = "google-subject-123", string? avatarUrl = "https://lh3.googleusercontent.com/a/photo") => new()
    {
        Provider = "Google",
        Subject = subject,
        Email = email,
        FirstName = first,
        LastName = last,
        AvatarUrl = avatarUrl
    };

    [Test]
    public async Task SignIn_ExistingUser_LinksByEmailAndKeepsTheirExistingRole()
    {
        var response = await _service.SignInAsync(Identity("Ada@Example.com"));

        response.Email.Should().Be("Ada@Example.com");
        // Crucially ADMIN, not CUSTOMER: linking must never downgrade (or upgrade) an existing account.
        response.Role.Should().Be("ADMIN");
        response.Token.Should().Be("fake-token");
        _context.Users.Count().Should().Be(2, "no new account should be created for a known email");
    }

    [Test]
    public async Task SignIn_ExistingUser_MatchesEmailCaseInsensitively()
    {
        // Google returns lower-cased addresses; the row was registered as "Ada@Example.com".
        // An exact-match lookup would miss it and silently provision a duplicate account.
        var response = await _service.SignInAsync(Identity("ada@example.com"));

        response.UserId.Should().Be(_context.Users.Single(u => u.Email == "Ada@Example.com").UserId);
        _context.Users.Count().Should().Be(2);
    }

    [Test]
    public async Task SignIn_UnknownEmail_ProvisionsCustomerWithMatchingProfile()
    {
        var response = await _service.SignInAsync(Identity("grace@example.com"));

        response.Role.Should().Be("CUSTOMER");
        response.FirstName.Should().Be("Grace");

        var created = await _context.Users.SingleAsync(u => u.Email == "grace@example.com");
        created.Status.Should().BeTrue();
        created.RoleId.Should().Be(_customerRoleId);

        // Without this row, the new user's first booking/cart/review would fail.
        var customer = await _context.Customers.SingleAsync(c => c.UserId == created.UserId);
        customer.FullName.Should().Be("Grace Hopper");
        customer.Email.Should().Be("grace@example.com");
    }

    [Test]
    public async Task SignIn_ProvisionedUser_CannotThenLogInWithAnEmptyOrGuessablePassword()
    {
        await _service.SignInAsync(Identity("grace@example.com"));
        var created = await _context.Users.SingleAsync(u => u.Email == "grace@example.com");

        // The column is NOT NULL so something must be stored; it must not be anything a caller
        // could supply to /api/auth/login and have BCrypt.Verify accept.
        created.PasswordHash.Should().NotBeNullOrWhiteSpace();
        BCrypt.Net.BCrypt.Verify("", created.PasswordHash).Should().BeFalse();
        BCrypt.Net.BCrypt.Verify("password", created.PasswordHash).Should().BeFalse();
        BCrypt.Net.BCrypt.Verify(created.Email, created.PasswordHash).Should().BeFalse();
    }

    [Test]
    public async Task SignIn_SecondTimeForTheSameUser_DoesNotCreateADuplicate()
    {
        await _service.SignInAsync(Identity("grace@example.com"));
        await _service.SignInAsync(Identity("grace@example.com"));

        _context.Users.Count(u => u.Email == "grace@example.com").Should().Be(1);
        _context.Customers.Count().Should().Be(1);
    }

    [Test]
    public async Task SignIn_DisabledAccount_IsRejectedJustLikePasswordLogin()
    {
        var act = async () => await _service.SignInAsync(Identity("disabled@example.com"));

        await act.Should().ThrowAsync<InvalidCredentialsException>();
    }

    [Test]
    public async Task SignIn_ProviderReturnedNoEmail_IsRejectedRatherThanProvisioningABlankAccount()
    {
        var act = async () => await _service.SignInAsync(Identity(""));

        await act.Should().ThrowAsync<ArgumentException>();
        _context.Users.Count().Should().Be(2);
    }

    [Test]
    public async Task SignIn_ProviderOmittedTheName_FallsBackToTheEmailLocalPart()
    {
        var response = await _service.SignInAsync(Identity("noname@example.com", first: null, last: null));

        // FirstName is NOT NULL in the schema, so it needs some sensible non-empty value.
        response.FirstName.Should().Be("noname");
    }

    // ----- Federated-identity columns, mirroring GoogleUserProvisioningService on the Java side.
    // Both backends write the same shared `users` table, so these must agree or an account's
    // provenance depends on which backend happened to be running when it was created.

    [Test]
    public async Task SignIn_UnknownEmail_StampsTheProviderIdentityOnTheNewRow()
    {
        await _service.SignInAsync(Identity("grace@example.com"));

        var created = await _context.Users.SingleAsync(u => u.Email == "grace@example.com");
        created.AuthProvider.Should().Be("GOOGLE");
        created.GoogleSub.Should().Be("google-subject-123");
        created.AvatarUrl.Should().Be("https://lh3.googleusercontent.com/a/photo");
    }

    [Test]
    public async Task SignIn_UnknownEmail_GivesTheCustomerAPlaceholderPhone()
    {
        // customer.phone is NOT NULL in the shared schema and no provider supplies one. Copying
        // the (always null) user phone aborted the insert, so a first-ever Google sign-up failed.
        // Ten digits also satisfies the Java side's ^[0-9]{10}$ validator on the same column.
        await _service.SignInAsync(Identity("grace@example.com"));

        var created = await _context.Users.SingleAsync(u => u.Email == "grace@example.com");
        var customer = await _context.Customers.SingleAsync(c => c.UserId == created.UserId);
        customer.Phone.Should().Be("0000000000");
        customer.Phone.Should().MatchRegex("^[0-9]{10}$");
    }

    [Test]
    public async Task SignIn_ExistingPasswordAccount_RecordsTheGoogleSubButStaysMarkedLocal()
    {
        // Linking must not rewrite how the account was created: it can now be reached both ways,
        // and overwriting the marker would lose that fact.
        await _service.SignInAsync(Identity("Ada@Example.com"));

        var linked = await _context.Users.SingleAsync(u => u.Email == "Ada@Example.com");
        linked.AuthProvider.Should().Be("LOCAL");
        linked.GoogleSub.Should().Be("google-subject-123");
        linked.AvatarUrl.Should().Be("https://lh3.googleusercontent.com/a/photo");
    }

    [Test]
    public async Task SignIn_ExistingAccount_LinkingNeverChangesThePasswordRoleOrStatus()
    {
        var before = await _context.Users.AsNoTracking().SingleAsync(u => u.Email == "Ada@Example.com");

        await _service.SignInAsync(Identity("Ada@Example.com"));

        var after = await _context.Users.SingleAsync(u => u.Email == "Ada@Example.com");
        after.PasswordHash.Should().Be(before.PasswordHash);
        after.RoleId.Should().Be(before.RoleId);
        after.Status.Should().Be(before.Status);
    }

    [Test]
    public async Task SignIn_ProviderSuppliedNoPicture_LeavesTheStoredAvatarAlone()
    {
        await _service.SignInAsync(Identity("Ada@Example.com"));

        await _service.SignInAsync(Identity("Ada@Example.com", avatarUrl: null));

        var linked = await _context.Users.SingleAsync(u => u.Email == "Ada@Example.com");
        linked.AvatarUrl.Should().Be("https://lh3.googleusercontent.com/a/photo");
    }

    private class FakeJwtTokenService : IJwtTokenService
    {
        public string GenerateToken(string email) => "fake-token";
    }
}
