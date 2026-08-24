using eTour.Application.Dtos;
using eTour.Application.Pricing;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Pricing;

[TestFixture]
public class TourPricingCalculatorTests
{
    private readonly TourPricingCalculator _calculator = new();
    private static readonly DateOnly DepartureDate = new(2027, 1, 1);

    private static PassengerInput Adult(string name = "Adult", Occupancy? occupancy = null) => new()
    {
        FullName = name,
        Dob = new DateOnly(1990, 1, 1),
        Occupancy = occupancy
    };

    private static PassengerInput Child(string name = "Child", Occupancy? occupancy = null, bool? needsExtraBed = null) => new()
    {
        FullName = name,
        Dob = new DateOnly(2020, 1, 1), // 7 years old at departure - CHILD band
        Occupancy = occupancy,
        NeedsExtraBed = needsExtraBed
    };

    private static PassengerInput Infant(string name = "Infant") => new()
    {
        FullName = name,
        Dob = new DateOnly(2026, 6, 1) // ~7 months old at departure - INFANT band
    };

    private static TourCost Cost(decimal basePrice = 20000m, decimal? single = 25000m, decimal? extra = 15000m,
        decimal? childWithBed = 10000m, decimal? childWithoutBed = 8000m) => new()
    {
        BasePrice = basePrice,
        SinglePersonCost = single,
        ExtraPersonCost = extra,
        ChildWithBedCost = childWithBed,
        ChildWithoutBedCost = childWithoutBed,
        ValidFrom = DepartureDate.AddYears(-1),
        ValidTo = DepartureDate.AddYears(1)
    };

    [Test]
    public void SingleAdult_NoExplicitOccupancy_DefaultsToSingleSupplementRate()
    {
        var result = _calculator.Calculate([Adult()], DepartureDate, Cost(), 20000m);

        result.Passengers.Single().Occupancy.Should().Be(Occupancy.SINGLE);
        result.Passengers.Single().CategoryRate.Should().Be(25000m);
        result.PassengersTotal.Should().Be(25000m);
    }

    [Test]
    public void TwoAdults_NoExplicitOccupancy_BothPairIntoTwin()
    {
        var result = _calculator.Calculate([Adult("A1"), Adult("A2")], DepartureDate, Cost(), 20000m);

        result.Passengers.Should().OnlyContain(p => p.Occupancy == Occupancy.TWIN && p.CategoryRate == 20000m);
        result.PassengersTotal.Should().Be(40000m);
        result.RoomSummary.DoubleRooms.Should().Be(1);
    }

    [Test]
    public void ThreeAdults_NoExplicitOccupancy_TwoTwinOneSingle()
    {
        var result = _calculator.Calculate([Adult("A1"), Adult("A2"), Adult("A3")], DepartureDate, Cost(), 20000m);

        result.Passengers[0].Occupancy.Should().Be(Occupancy.TWIN);
        result.Passengers[1].Occupancy.Should().Be(Occupancy.TWIN);
        result.Passengers[2].Occupancy.Should().Be(Occupancy.SINGLE);
        result.PassengersTotal.Should().Be(20000m + 20000m + 25000m);
    }

    [Test]
    public void OneExplicitAdultChoice_MakesUnspecifiedAdultsDefaultToTwin_NotPaired()
    {
        // Legacy pairing is bypassed entirely once ANY adult states a preference - the other
        // adult defaults to TWIN even though, under legacy pairing, a lone unspecified adult
        // would have been priced SINGLE.
        var result = _calculator.Calculate([Adult("A1", Occupancy.SINGLE), Adult("A2")], DepartureDate, Cost(), 20000m);

        result.Passengers[0].Occupancy.Should().Be(Occupancy.SINGLE);
        result.Passengers[1].Occupancy.Should().Be(Occupancy.TWIN);
    }

    [Test]
    public void Child_NeedsExtraBedTrue_PricesWithBed()
    {
        var result = _calculator.Calculate([Adult(), Child(needsExtraBed: true)], DepartureDate, Cost(), 20000m);
        result.Passengers[1].Occupancy.Should().Be(Occupancy.CHILD_WITH_BED);
        result.Passengers[1].CategoryRate.Should().Be(10000m);
    }

    [Test]
    public void Child_NeedsExtraBedFalse_PricesWithoutBed()
    {
        var result = _calculator.Calculate([Adult(), Child(needsExtraBed: false)], DepartureDate, Cost(), 20000m);
        result.Passengers[1].Occupancy.Should().Be(Occupancy.CHILD_WITHOUT_BED);
        result.Passengers[1].CategoryRate.Should().Be(8000m);
    }

    [Test]
    public void Child_NeedsExtraBedNull_DefaultsToWithBed()
    {
        var result = _calculator.Calculate([Adult(), Child(needsExtraBed: null)], DepartureDate, Cost(), 20000m);
        result.Passengers[1].Occupancy.Should().Be(Occupancy.CHILD_WITH_BED);
    }

    [Test]
    public void Infant_IsFreeAndHasNoOccupancy()
    {
        var result = _calculator.Calculate([Adult(), Infant()], DepartureDate, Cost(), 20000m);

        var infantLine = result.Passengers[1];
        infantLine.Occupancy.Should().BeNull();
        infantLine.Price.Should().Be(0m);
        result.RoomSummary.Infants.Should().Be(1);
    }

    [Test]
    public void OccupancyContradictingDerivedBand_ThrowsIllegalOperationException()
    {
        // A passenger whose DOB makes them a CHILD cannot be assigned an adult-only category (TWIN).
        var act = () => _calculator.Calculate([Child("Kid", Occupancy.TWIN)], DepartureDate, Cost(), 20000m);

        act.Should().Throw<IllegalOperationException>().WithMessage("*cannot use*");
    }

    [Test]
    public void NoTourCost_FallsBackToFlatSchedulePrice_ChildrenHalfPrice_InfantsFree()
    {
        var result = _calculator.Calculate([Adult(), Child(needsExtraBed: true), Infant()], DepartureDate, null, 10000m);

        result.Passengers[0].CategoryRate.Should().Be(10000m); // adult - flat price
        result.Passengers[1].CategoryRate.Should().Be(5000m); // child - half price
        result.Passengers[2].Price.Should().Be(0m); // infant - free
    }

    [Test]
    public void RoomCharge_OnlyAppliedWhenOccupancyExplicitlyChosen()
    {
        var roomCharges = new Dictionary<Occupancy, decimal> { [Occupancy.SINGLE] = 3000m };

        var explicitChoice = _calculator.Calculate([Adult("A1", Occupancy.SINGLE)], DepartureDate, Cost(), 20000m, roomCharges);
        explicitChoice.Passengers.Single().RoomCharge.Should().Be(3000m);
        explicitChoice.Passengers.Single().Price.Should().Be(28000m); // 25000 + 3000

        // A single lone adult with NO explicit choice is still priced SINGLE by the legacy
        // pairing rule, but since it wasn't an explicit choice, no supplement applies.
        var inferredChoice = _calculator.Calculate([Adult("A1")], DepartureDate, Cost(), 20000m, roomCharges);
        inferredChoice.Passengers.Single().Occupancy.Should().Be(Occupancy.SINGLE);
        inferredChoice.Passengers.Single().RoomCharge.Should().Be(0m);
    }

    [Test]
    public void AmountsRoundHalfUp_ToTwoDecimalPlaces()
    {
        var cost = Cost(basePrice: 100.005m, single: null, extra: null, childWithBed: null, childWithoutBed: null);
        var result = _calculator.Calculate([Adult()], DepartureDate, cost, 100m, new Dictionary<Occupancy, decimal>());

        // Lone adult -> SINGLE -> falls back to TWIN rate (basePrice) since SinglePersonCost is null.
        result.Passengers.Single().Price.Should().Be(100.01m);
    }

    [Test]
    public void Breakdown_GroupsPassengersByCategory_InDocumentedOrder()
    {
        var result = _calculator.Calculate(
            [Adult("A1"), Adult("A2"), Adult("A3", Occupancy.EXTRA_BED), Child("C1", needsExtraBed: true), Infant()],
            DepartureDate, Cost(), 20000m);

        result.Breakdown.Select(b => b.Label).Should().ContainInOrder(
            "Adults (twin-sharing)", "Extra person", "Child (with bed)", "Infant");
    }
}
