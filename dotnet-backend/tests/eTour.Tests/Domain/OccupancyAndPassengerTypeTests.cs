using eTour.Domain.Enums;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Domain;

[TestFixture]
public class OccupancyAndPassengerTypeTests
{
    [TestCase(Occupancy.TWIN, PassengerType.ADULT, true)]
    [TestCase(Occupancy.SINGLE, PassengerType.ADULT, true)]
    [TestCase(Occupancy.EXTRA_BED, PassengerType.ADULT, true)]
    [TestCase(Occupancy.CHILD_WITH_BED, PassengerType.ADULT, false)]
    [TestCase(Occupancy.CHILD_WITH_BED, PassengerType.CHILD, true)]
    [TestCase(Occupancy.CHILD_WITHOUT_BED, PassengerType.CHILD, true)]
    [TestCase(Occupancy.TWIN, PassengerType.CHILD, false)]
    [TestCase(Occupancy.TWIN, PassengerType.INFANT, false)]
    [TestCase(Occupancy.CHILD_WITH_BED, PassengerType.INFANT, false)]
    public void AppliesTo_EnforcesAdultVsChildCategorySeparation(Occupancy occupancy, PassengerType type, bool expected)
    {
        occupancy.AppliesTo(type).Should().Be(expected);
    }

    [Test]
    public void AppliesTo_NullType_AlwaysTrue()
    {
        Occupancy.CHILD_WITH_BED.AppliesTo(null).Should().BeTrue();
    }

    [TestCase(0, PassengerType.INFANT)] // newborn
    [TestCase(1, PassengerType.INFANT)]
    [TestCase(2, PassengerType.CHILD)]
    [TestCase(11, PassengerType.CHILD)]
    [TestCase(12, PassengerType.ADULT)]
    [TestCase(40, PassengerType.ADULT)]
    public void FromAge_BandsByAgeAtDeparture(int ageAtDeparture, PassengerType expected)
    {
        var departureDate = new DateOnly(2027, 6, 15);
        var dob = departureDate.AddYears(-ageAtDeparture);

        PassengerTypeExtensions.FromAge(dob, departureDate).Should().Be(expected);
    }

    [Test]
    public void FromAge_NullDob_DefaultsToAdult()
    {
        PassengerTypeExtensions.FromAge(null, new DateOnly(2027, 1, 1)).Should().Be(PassengerType.ADULT);
    }

    [Test]
    public void FromAge_NullDepartureDate_DefaultsToAdult()
    {
        PassengerTypeExtensions.FromAge(new DateOnly(2020, 1, 1), null).Should().Be(PassengerType.ADULT);
    }

    [Test]
    public void DefaultFor_Child_NeedsExtraBedNullOrTrue_GivesWithBed()
    {
        OccupancyExtensions.DefaultFor(PassengerType.CHILD, null).Should().Be(Occupancy.CHILD_WITH_BED);
        OccupancyExtensions.DefaultFor(PassengerType.CHILD, true).Should().Be(Occupancy.CHILD_WITH_BED);
        OccupancyExtensions.DefaultFor(PassengerType.CHILD, false).Should().Be(Occupancy.CHILD_WITHOUT_BED);
    }

    [Test]
    public void DefaultFor_Infant_IsNull()
    {
        OccupancyExtensions.DefaultFor(PassengerType.INFANT, true).Should().BeNull();
    }

    [Test]
    public void DefaultFor_Adult_IsTwin()
    {
        OccupancyExtensions.DefaultFor(PassengerType.ADULT, null).Should().Be(Occupancy.TWIN);
    }
}
