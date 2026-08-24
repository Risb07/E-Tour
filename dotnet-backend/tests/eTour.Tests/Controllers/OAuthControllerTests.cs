using System.Net;
using System.Net.Http.Json;
using eTour.Application.Dtos;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.Extensions.DependencyInjection;
using NUnit.Framework;

namespace eTour.Tests.Controllers;

/// <summary>
/// Full-pipeline checks for the OAuth routes with NO provider configured - which is the state the
/// app ships in, and the state these tests run in. The point is that adding OAuth changed nothing
/// for anyone who does not use it: the app still boots, the existing auth still works, and the
/// unconfigured routes fail cleanly instead of 500-ing or taking the process down at startup.
/// </summary>
[TestFixture]
public class OAuthControllerTests
{
    private ApiTestFactory _factory = null!;
    private HttpClient _client = null!;

    [SetUp]
    public void SetUp()
    {
        _factory = new ApiTestFactory();
        // Redirects are followed by default, which would turn a 302-to-Google into a real network
        // call; this keeps the assertions on what our own server returned.
        _client = _factory.CreateClient(new Microsoft.AspNetCore.Mvc.Testing.WebApplicationFactoryClientOptions
        {
            AllowAutoRedirect = false
        });
    }

    [TearDown]
    public void TearDown()
    {
        _client.Dispose();
        _factory.Dispose();
    }

    [Test]
    public async Task AppStartsAndServesRequests_WithNoOAuthConfiguration()
    {
        // The Google handler throws on a blank client id, so registering it unconditionally would
        // make an unconfigured deployment fail at startup rather than merely lack the feature.
        var response = await _client.GetAsync("/api/tours");

        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Test]
    public async Task StartGoogle_WhenNotConfigured_Returns503_NotACrash()
    {
        var response = await _client.GetAsync("/api/auth/oauth/google");

        response.StatusCode.Should().Be(HttpStatusCode.ServiceUnavailable);

        // Same error envelope as every other failure in the API.
        var body = await response.Content.ReadFromJsonAsync<ApiErrorEnvelope>();
        body.Should().NotBeNull();
        body!.Status.Should().Be(503);
        body.Message.Should().Contain("not configured");
    }

    [Test]
    public async Task Callback_WhenNotConfigured_Returns503()
    {
        var response = await _client.GetAsync("/api/auth/oauth/google/callback");

        response.StatusCode.Should().Be(HttpStatusCode.ServiceUnavailable);
    }

    [Test]
    public async Task OAuthRoutes_ArePublic_NotCaughtByTheDefaultDenyPolicy()
    {
        // A 401 here would mean [AllowAnonymous] was missing and the fallback policy had swallowed
        // the route - the user could never reach the sign-in redirect in the first place.
        var response = await _client.GetAsync("/api/auth/oauth/google");

        response.StatusCode.Should().NotBe(HttpStatusCode.Unauthorized);
    }

    [Test]
    public async Task ExistingPasswordLogin_StillWorks_WithOAuthCodePresent()
    {
        // The regression guard for "without changing any previous functionality": JWT bearer must
        // still be the default scheme, so register -> login -> authenticated call is unaffected.
        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<EtourDbContext>();
            db.Roles.Add(new eTour.Domain.Entities.Role { RoleName = "CUSTOMER", Status = true });
            await db.SaveChangesAsync();
        }

        var register = await _client.PostAsJsonAsync("/api/users/register", new RegisterRequest
        {
            FirstName = "OAuth",
            LastName = "Bystander",
            Email = "bystander@example.com",
            Password = "Sup3rSecret!",
            Phone = "9876543210"
        });
        register.StatusCode.Should().Be(HttpStatusCode.Created);

        var login = await _client.PostAsJsonAsync("/api/auth/login", new LoginRequest
        {
            Email = "bystander@example.com",
            Password = "Sup3rSecret!"
        });
        login.StatusCode.Should().Be(HttpStatusCode.OK);

        var body = await login.Content.ReadFromJsonAsync<LoginResponse>();
        body!.Token.Should().NotBeNullOrWhiteSpace();

        using var authed = _factory.CreateClient();
        authed.DefaultRequestHeaders.Authorization =
            new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", body.Token);
        (await authed.GetAsync("/api/bookings/me")).StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Test]
    public async Task Me_WithoutAToken_IsRejectedByTheAuthPipeline_NotJustByTheServiceThrowing()
    {
        // It resolves the caller's identity, so it must never be reachable anonymously. Asserting
        // the MESSAGE matters as much as the status: AuthController used to carry a class-level
        // [AllowAnonymous], which silently overrode this action's [Authorize] (ASP0026). The
        // endpoint still answered 401 - but only because ICurrentUserService threw once inside the
        // action, i.e. the request had already been let through. This is the message the JWT
        // handler's OnChallenge writes, so it only appears when auth rejected the request first.
        var response = await _client.GetAsync("/api/auth/me");

        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);

        var body = await response.Content.ReadFromJsonAsync<ApiErrorEnvelope>();
        body!.Message.Should().Be("Authentication is required to access this resource");
    }

    [Test]
    public async Task Me_WithAToken_ReturnsTheProfileTheOAuthCallbackNeeds()
    {
        // The OAuth redirect can only carry the token, so this endpoint is the sole route by which
        // the client learns who signed in - if its shape drifts, the callback cannot build a session.
        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<EtourDbContext>();
            db.Roles.Add(new eTour.Domain.Entities.Role { RoleName = "CUSTOMER", Status = true });
            await db.SaveChangesAsync();
        }

        var register = await _client.PostAsJsonAsync("/api/users/register", new RegisterRequest
        {
            FirstName = "Grace",
            LastName = "Hopper",
            Email = "grace@example.com",
            Password = "Sup3rSecret!"
        });
        var registered = await register.Content.ReadFromJsonAsync<RegisterResponse>();

        using var authed = _factory.CreateClient();
        authed.DefaultRequestHeaders.Authorization =
            new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", registered!.Token);

        var response = await authed.GetAsync("/api/auth/me");
        response.StatusCode.Should().Be(HttpStatusCode.OK);

        var me = await response.Content.ReadFromJsonAsync<CurrentUserResponse>();
        me.Should().NotBeNull();
        me!.Email.Should().Be("grace@example.com");
        me.FirstName.Should().Be("Grace");
        me.Role.Should().Be("CUSTOMER");
        me.UserId.Should().Be(registered.UserId);
    }

    private record ApiErrorEnvelope(DateTime Timestamp, int Status, string Message, string Path);
}
