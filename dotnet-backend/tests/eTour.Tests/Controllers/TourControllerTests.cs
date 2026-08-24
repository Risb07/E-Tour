using System.Net;
using System.Net.Http.Json;
using eTour.Application.Dtos;
using eTour.Tests.TestSupport;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Controllers;

[TestFixture]
public class TourControllerTests
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
    public async Task GetAllTours_IsPublic_ReturnsOkWithoutAuthentication()
    {
        var response = await _client.GetAsync("/api/tours");
        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Test]
    public async Task CreateTour_WithoutAuthentication_Returns401()
    {
        var response = await _client.PostAsJsonAsync("/api/tours", new TourRequest
        {
            Title = "Test",
            DurationDays = 3,
            BasePrice = 1000,
            TourCode = "DOM",
            Status = "DRAFT"
        });

        // Fallback authorization policy denies by default - matches Java's anyRequest().authenticated().
        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
    }

    [Test]
    public async Task GetTourById_UnknownId_ReturnsConsistentErrorEnvelope()
    {
        var response = await _client.GetAsync("/api/tours/999999");

        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
        var body = await response.Content.ReadFromJsonAsync<ApiErrorEnvelope>();
        body.Should().NotBeNull();
        body!.Status.Should().Be(404);
        body.Message.Should().NotBeNullOrEmpty();
    }

    private record ApiErrorEnvelope(DateTime Timestamp, int Status, string Message, string Path);
}
