using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Pricing;

/// <summary>One passenger after the server has decided how they are actually being priced.</summary>
public class ResolvedPassenger
{
    public ResolvedPassenger(int index, string fullName, PassengerType type, Occupancy? occupancy,
        decimal categoryRate, decimal roomCharge, decimal price)
    {
        Index = index;
        FullName = fullName;
        Type = type;
        Occupancy = occupancy;
        CategoryRate = categoryRate;
        RoomCharge = roomCharge;
        Price = price;
    }

    public int Index { get; }
    public string FullName { get; }
    public PassengerType Type { get; }
    /// <summary>Null for infants.</summary>
    public Occupancy? Occupancy { get; }
    public decimal CategoryRate { get; }
    public decimal RoomCharge { get; }
    public decimal Price { get; }

    // Goes through the constructor rather than an object initialiser so the enum name/label pair
    // is always derived together - a line can't end up with a type but no label.
    public PassengerPriceLine ToPriceLine() =>
        new(Index, FullName, Type, Occupancy, CategoryRate, RoomCharge, Price);
}

public class PricingResult
{
    public PricingResult(RoomSummary roomSummary, List<CostBreakdownLine> breakdown, decimal passengersTotal,
        List<ResolvedPassenger>? passengers = null)
    {
        RoomSummary = roomSummary;
        Breakdown = breakdown;
        PassengersTotal = passengersTotal;
        Passengers = passengers ?? [];
    }

    public RoomSummary RoomSummary { get; }
    public List<CostBreakdownLine> Breakdown { get; }
    public decimal PassengersTotal { get; }
    /// <summary>Per-passenger detail, in submitted order. Never null.</summary>
    public List<ResolvedPassenger> Passengers { get; }

    public List<PassengerPriceLine> PassengerLines() => Passengers.Select(p => p.ToPriceLine()).ToList();
}

/// <summary>
/// BRD 3.7 "Book Tour" pricing - ported 1:1 from Java's TourPricingCalculator. Date of birth is
/// the single source of truth for the passenger band (age at departure decides it; the client's
/// declared type is never honoured), and an occupancy that contradicts the derived band is
/// rejected rather than silently repriced. See eTour.Domain.Enums.Occupancy/PassengerType for the
/// exact category <-> TourCost column mapping and age boundaries.
/// </summary>
public class TourPricingCalculator
{
    public PricingResult Calculate(IReadOnlyList<PassengerInput> passengers, DateOnly departureDate, TourCost? cost,
        decimal fallbackPerSeatPrice, IReadOnlyDictionary<Occupancy, decimal>? roomCharges = null)
    {
        roomCharges ??= new Dictionary<Occupancy, decimal>();

        // --- Pass 1: decide each passenger's type and occupancy category ----

        var count = passengers.Count;
        var types = new PassengerType[count];
        var occupancies = new Occupancy?[count];
        // Whether the CLIENT chose the category (vs the server inferring it). Only explicit
        // choices attract a room supplement.
        var explicitChoice = new bool[count];

        var adults = 0;
        var children = 0;
        var infants = 0;
        var anyExplicitAdultOccupancy = false;

        for (var i = 0; i < count; i++)
        {
            var p = passengers[i];
            // DOB decides the band, always - whatever passengerType the request carried is ignored
            // on purpose, since honouring it would make the category a client-controlled input again.
            var type = PassengerTypeExtensions.FromAge(p.Dob, departureDate);
            types[i] = type;

            var requested = p.Occupancy;
            if (requested is not null && !requested.Value.AppliesTo(type))
            {
                throw new IllegalOperationException(
                    $"Passenger {i + 1} ({SafeName(p)}) is a {type.GetLabel().ToLower()} by date of birth and cannot use the \"{requested.Value.GetLabel()}\" category");
            }

            switch (type)
            {
                case PassengerType.INFANT:
                    infants++;
                    occupancies[i] = null; // free, and occupies no bed
                    break;
                case PassengerType.CHILD:
                    children++;
                    if (requested is not null)
                    {
                        occupancies[i] = requested;
                        explicitChoice[i] = true;
                    }
                    else
                    {
                        occupancies[i] = OccupancyExtensions.DefaultFor(type, p.NeedsExtraBed);
                    }
                    break;
                case PassengerType.ADULT:
                    adults++;
                    if (requested is not null)
                    {
                        occupancies[i] = requested;
                        explicitChoice[i] = true;
                        anyExplicitAdultOccupancy = true;
                    }
                    // else: left null for now - resolved in pass 2, since it depends on whether
                    // ANY adult stated a preference.
                    break;
            }
        }

        // --- Pass 2: fill in the adults nobody chose a category for ---------

        if (anyExplicitAdultOccupancy)
        {
            for (var i = 0; i < count; i++)
            {
                if (types[i] == PassengerType.ADULT && occupancies[i] is null)
                {
                    occupancies[i] = Occupancy.TWIN;
                }
            }
        }
        else
        {
            // Legacy/auto behaviour: pair adults into twins, odd one out pays the single supplement.
            var assigned = 0;
            var adultsInTwins = adults / 2 * 2;
            for (var i = 0; i < count; i++)
            {
                if (types[i] != PassengerType.ADULT)
                {
                    continue;
                }
                occupancies[i] = assigned < adultsInTwins ? Occupancy.TWIN : Occupancy.SINGLE;
                assigned++;
            }
        }

        // --- Pass 3: rates ---------------------------------------------------

        decimal adultTwinRate, adultSingleRate, adultExtraRate, childWithBedRate, childWithoutBedRate;

        if (cost is not null)
        {
            adultTwinRate = cost.BasePrice ?? fallbackPerSeatPrice;
            adultSingleRate = cost.SinglePersonCost ?? adultTwinRate;
            adultExtraRate = cost.ExtraPersonCost ?? adultTwinRate;
            childWithBedRate = cost.ChildWithBedCost ?? Half(fallbackPerSeatPrice);
            childWithoutBedRate = cost.ChildWithoutBedCost ?? Half(fallbackPerSeatPrice);
        }
        else
        {
            adultTwinRate = fallbackPerSeatPrice;
            adultSingleRate = fallbackPerSeatPrice; // no configured single-room surcharge available
            adultExtraRate = fallbackPerSeatPrice;
            childWithBedRate = Half(fallbackPerSeatPrice);
            childWithoutBedRate = Half(fallbackPerSeatPrice);
        }

        // --- Pass 4: price each passenger, and tally the grouped view -------

        var twinAdults = 0;
        var singleAdults = 0;
        var extraBedAdults = 0;
        var childrenWithBed = 0;
        var childrenWithoutBed = 0;
        var supplementCounts = new Dictionary<Occupancy, int>();

        var resolved = new List<ResolvedPassenger>(count);
        var total = 0m;

        for (var i = 0; i < count; i++)
        {
            var occupancy = occupancies[i];
            // Pass 2 guarantees every adult has a category by now.
            var categoryRate = types[i] switch
            {
                PassengerType.INFANT => 0m,
                PassengerType.CHILD => occupancy == Occupancy.CHILD_WITHOUT_BED ? childWithoutBedRate : childWithBedRate,
                PassengerType.ADULT => occupancy == Occupancy.SINGLE
                    ? adultSingleRate
                    : occupancy is Occupancy.TRIPLE or Occupancy.EXTRA_BED
                        ? adultExtraRate
                        : adultTwinRate,
                _ => adultTwinRate
            };

            var supplement = 0m;
            if (explicitChoice[i] && occupancy is not null && roomCharges.TryGetValue(occupancy.Value, out var configured) && configured > 0)
            {
                supplement = configured;
                supplementCounts[occupancy.Value] = supplementCounts.GetValueOrDefault(occupancy.Value) + 1;
            }

            var price = categoryRate + supplement;
            total += price;
            resolved.Add(new ResolvedPassenger(i, SafeName(passengers[i]), types[i], occupancy, categoryRate, supplement,
                Math.Round(price, 2, MidpointRounding.AwayFromZero)));

            switch (types[i])
            {
                case PassengerType.INFANT:
                    break; // counted above, contributes nothing
                case PassengerType.CHILD:
                    if (occupancy == Occupancy.CHILD_WITHOUT_BED)
                    {
                        childrenWithoutBed++;
                    }
                    else
                    {
                        childrenWithBed++;
                    }
                    break;
                case PassengerType.ADULT:
                    if (occupancy == Occupancy.SINGLE)
                    {
                        singleAdults++;
                    }
                    else if (occupancy is Occupancy.TRIPLE or Occupancy.EXTRA_BED)
                    {
                        extraBedAdults++;
                    }
                    else
                    {
                        twinAdults++;
                    }
                    break;
            }
        }

        // --- Pass 5: the grouped breakdown, unchanged in shape and order ----

        var lines = new List<CostBreakdownLine>();
        if (twinAdults > 0)
        {
            lines.Add(Line("Adults (twin-sharing)", adultTwinRate, twinAdults));
        }
        if (singleAdults > 0)
        {
            lines.Add(Line("Single occupancy", adultSingleRate, singleAdults));
        }
        if (extraBedAdults > 0)
        {
            lines.Add(Line("Extra person", adultExtraRate, extraBedAdults));
        }
        if (childrenWithBed > 0)
        {
            lines.Add(Line("Child (with bed)", childWithBedRate, childrenWithBed));
        }
        if (childrenWithoutBed > 0)
        {
            lines.Add(Line("Child (without bed)", childWithoutBedRate, childrenWithoutBed));
        }
        if (infants > 0)
        {
            lines.Add(new CostBreakdownLine("Infant", 0m, infants, 0m));
        }
        foreach (var (occupancy, qty) in supplementCounts)
        {
            lines.Add(Line($"{occupancy.GetLabel()} supplement", roomCharges[occupancy], qty));
        }

        // Twin-sharers pair up; an unpaired twin selection still needs a room, so round up.
        var doubleRooms = (twinAdults + 1) / 2;
        var roomSummary = new RoomSummary(adults, children, infants, doubleRooms, singleAdults, extraBedAdults);

        return new PricingResult(roomSummary, lines, Math.Round(total, 2, MidpointRounding.AwayFromZero), resolved);
    }

    private static CostBreakdownLine Line(string label, decimal unitPrice, int quantity) =>
        new(label, unitPrice, quantity, unitPrice * quantity);

    private static string SafeName(PassengerInput p) => p.FullName ?? "";

    private static decimal Half(decimal value) => Math.Round(value / 2, 2, MidpointRounding.AwayFromZero);
}
