using System.Text.Json;
using eTour.Application.Dtos;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Services;

/// <summary>
/// Pins the JSON property names of the payloads the existing React client reads, against the
/// names the Java backend emits (it serializes its own DTOs/entities, so those names ARE the
/// contract this client was built for).
///
/// A rename on either side is invisible to the compiler and to every behavioural test - the
/// endpoint still returns 200 with a well-formed body, the client just silently reads undefined
/// and renders a blank. That is exactly how the booking review screen ended up showing no
/// prices. These assertions are the only thing that catches it.
/// </summary>
[TestFixture]
public class WireContractTests
{
    private static readonly JsonSerializerOptions CamelCase =
        new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };

    private static IEnumerable<string> NamesOf(object dto) =>
        JsonSerializer.SerializeToElement(dto, CamelCase).EnumerateObject().Select(p => p.Name);

    private static void ShouldExpose(object dto, params string[] required) =>
        NamesOf(dto).Should().Contain(required,
            $"the client reads these off {dto.GetType().Name}");

    [Test]
    public void BookingQuoteResponse_ExposesTheNamesTheReviewScreenReads()
    {
        // totalAmount (not total) is the headline figure on the review step.
        ShouldExpose(new BookingQuoteResponse(),
            "scheduleId", "roomSummary", "breakdown", "passengerLines", "totalAmount");
    }

    [Test]
    public void CostBreakdownLine_ExposesLineTotal_NotTotal()
    {
        var names = NamesOf(new CostBreakdownLine("Twin sharing", 100m, 2, 200m)).ToList();
        names.Should().Contain(["label", "unitPrice", "quantity", "lineTotal"]);
        names.Should().NotContain("total");
    }

    [Test]
    public void RoomSummary_ExposesRoomCounts_NotPassengerCounts()
    {
        var names = NamesOf(new RoomSummary(2, 1, 0, 1, 1, 1)).ToList();
        names.Should().Contain(["adults", "children", "infants", "doubleRooms", "singleRooms", "extraBeds"]);
        names.Should().NotContain(["singleAdults", "extraBedAdults"]);
    }

    [Test]
    public void PassengerPriceLine_SendsBothTheEnumNameAndItsLabel()
    {
        var line = new PassengerPriceLine(0, "Ada", eTour.Domain.Enums.PassengerType.CHILD,
            eTour.Domain.Enums.Occupancy.CHILD_WITH_BED, 500m, 0m, 500m);

        ShouldExpose(line, "passengerIndex", "fullName",
            "passengerType", "passengerTypeLabel", "occupancy", "occupancyLabel",
            "categoryRate", "roomCharge", "price");

        // The labels are what the summary actually prints - they must be resolved, not the raw name.
        line.PassengerTypeLabel.Should().Be("Child");
        line.OccupancyLabel.Should().Be("Child with bed");
    }

    [Test]
    public void PassengerPriceLine_OmitsOccupancyLabelForAnInfant()
    {
        // Infants occupy no bed, so both occupancy fields stay null rather than defaulting.
        var line = new PassengerPriceLine(0, "Baby", eTour.Domain.Enums.PassengerType.INFANT,
            null, 0m, 0m, 0m);

        line.Occupancy.Should().BeNull();
        line.OccupancyLabel.Should().BeNull();
        line.PassengerTypeLabel.Should().Be("Infant");
    }

    [Test]
    public void ReviewSummary_ExposesTotalReviews_NotReviewCount()
    {
        var names = NamesOf(new ReviewSummary()).ToList();
        names.Should().Contain("totalReviews");
        names.Should().NotContain("reviewCount");
    }

    [Test]
    public void PaymentResponse_ExposesPaymentStatus_NotStatus()
    {
        var names = NamesOf(new PaymentResponse()).ToList();
        names.Should().Contain("paymentStatus");
        names.Should().NotContain("status");
    }

    [Test]
    public void PlannedChange_ExposesLinkTypeAndAlreadyLinked()
    {
        var names = NamesOf(new PlannedChange(1, "T", "R", "CATEGORY", "Europe", false)).ToList();
        names.Should().Contain(["linkType", "alreadyLinked"]);
        names.Should().NotContain(["pathType", "alreadyExists"]);
    }

    [Test]
    public void TourDetailDtos_ExposeResolvedLocationNames()
    {
        // Ids alone are useless to the itinerary/stay tabs - they have no location lookup to join on.
        ShouldExpose(new JourneyDetailDto(), "fromLocationName", "toLocationName");
        ShouldExpose(new StayMealDto(), "locationName");
    }

    [Test]
    public void WishlistItemResponse_ExposesTheFieldsTheCardsRender()
    {
        ShouldExpose(new WishlistItemResponse(), "tourCode", "durationDays", "tourTitle", "basePrice");
    }

    [Test]
    public void CardPaymentRequest_AcceptsSaveCard()
    {
        var parsed = JsonSerializer.Deserialize<CardPaymentRequest>(
            """{"bookingId":1,"cardNumber":"4111111111111111","cardHolderName":"A","expiryMonth":1,"expiryYear":2030,"cvv":"123","saveCard":true}""",
            CamelCase)!;

        parsed.SaveCard.Should().BeTrue();
    }
}
