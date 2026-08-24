using System.Net;
using System.Net.Http.Json;
using eTour.Application.Dtos;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.Extensions.DependencyInjection;
using NUnit.Framework;

namespace eTour.Tests.Controllers;

[TestFixture]
public class AuthControllerTests
{
    private ApiTestFactory _factory = null!;
    private HttpClient _client = null!;

    [SetUp]
    public void SetUp()
    {
        _factory = new ApiTestFactory();
        _client = _factory.CreateClient();
    }

    [TearDown]
    public void TearDown()
    {
        _client.Dispose();
        _factory.Dispose();
    }

    [Test]
    public async Task RegisterThenLogin_EndToEnd_IssuesUsableJwt()
    {
        // Seeding is disabled for tests, so the CUSTOMER role UserService.RegisterAsync depends
        // on must be seeded here directly.
        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<EtourDbContext>();
            db.Roles.Add(new eTour.Domain.Entities.Role { RoleName = "CUSTOMER", Status = true });
            await db.SaveChangesAsync();
        }

        var registerResponse = await _client.PostAsJsonAsync("/api/users/register", new RegisterRequest
        {
            FirstName = "Ada",
            LastName = "Lovelace",
            Email = "ada@example.com",
            Password = "Sup3rSecret!"
        });
        registerResponse.StatusCode.Should().Be(HttpStatusCode.Created);

        var loginResponse = await _client.PostAsJsonAsync("/api/auth/login", new LoginRequest
        {
            Email = "ada@example.com",
            Password = "Sup3rSecret!"
        });
        loginResponse.StatusCode.Should().Be(HttpStatusCode.OK);

        var login = await loginResponse.Content.ReadFromJsonAsync<LoginResponse>();
        login!.Token.Should().NotBeNullOrEmpty();
        login.Role.Should().Be("CUSTOMER");

        // The issued token should authenticate a subsequent request to a customer-only endpoint.
        _client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", login.Token);
        var meResponse = await _client.GetAsync("/api/customer/me");
        meResponse.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Test]
    public async Task Login_WrongPassword_Returns401WithGenericMessage()
    {
        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<EtourDbContext>();
            var role = new eTour.Domain.Entities.Role { RoleName = "CUSTOMER", Status = true };
            db.Roles.Add(role);
            await db.SaveChangesAsync();
            db.Users.Add(new eTour.Domain.Entities.User
            {
                FirstName = "Ada",
                LastName = "Lovelace",
                Email = "ada@example.com",
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("Correct123!"),
                Status = true,
                RoleId = role.RoleId
            });
            await db.SaveChangesAsync();
        }

        var response = await _client.PostAsJsonAsync("/api/auth/login", new LoginRequest { Email = "ada@example.com", Password = "wrong" });

        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        var body = await response.Content.ReadAsStringAsync();
        body.Should().Contain("Invalid email or password");
    }
}
