using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.Extensions.Logging.Abstractions;
using NUnit.Framework;

namespace eTour.Tests.Services;

[TestFixture]
public class AuthServiceTests
{
    private EtourDbContext _context = null!;
    private AuthService _service = null!;
    private const string Password = "Sup3rSecret!";

    [SetUp]
    public async Task SetUp()
    {
        _context = TestDbContextFactory.Create();

        var role = new Role { RoleName = "CUSTOMER", Status = true };
        _context.Roles.Add(role);
        await _context.SaveChangesAsync();

        _context.Users.Add(new User
        {
            FirstName = "Ada",
            LastName = "Lovelace",
            Email = "ada@example.com",
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(Password),
            Status = true,
            RoleId = role.RoleId
        });
        _context.Users.Add(new User
        {
            FirstName = "Disabled",
            LastName = "User",
            Email = "disabled@example.com",
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(Password),
            Status = false,
            RoleId = role.RoleId
        });
        await _context.SaveChangesAsync();

        var userRepository = new GenericRepository<User, long>(_context);
        var jwtTokenService = new FakeJwtTokenService();
        _service = new AuthService(userRepository, jwtTokenService, NullLogger<AuthService>.Instance);
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    [Test]
    public async Task Login_CorrectCredentials_ReturnsTokenAndUserInfo()
    {
        var response = await _service.LoginAsync(new LoginRequest { Email = "ada@example.com", Password = Password });

        response.Token.Should().Be("fake-token");
        response.Email.Should().Be("ada@example.com");
        response.Role.Should().Be("CUSTOMER");
    }

    [Test]
    public async Task Login_WrongPassword_ThrowsInvalidCredentials_NotResourceNotFound()
    {
        var act = async () => await _service.LoginAsync(new LoginRequest { Email = "ada@example.com", Password = "wrong" });
        await act.Should().ThrowAsync<InvalidCredentialsException>();
    }

    [Test]
    public async Task Login_UnknownEmail_ThrowsSameExceptionAsWrongPassword()
    {
        // Same exception type/message for "no such user" and "wrong password" - never confirm an email exists.
        var act = async () => await _service.LoginAsync(new LoginRequest { Email = "nobody@example.com", Password = Password });
        await act.Should().ThrowAsync<InvalidCredentialsException>();
    }

    [Test]
    public async Task Login_DisabledUser_Rejected()
    {
        var act = async () => await _service.LoginAsync(new LoginRequest { Email = "disabled@example.com", Password = Password });
        await act.Should().ThrowAsync<InvalidCredentialsException>();
    }

    private class FakeJwtTokenService : IJwtTokenService
    {
        public string GenerateToken(string email) => "fake-token";
    }
}
