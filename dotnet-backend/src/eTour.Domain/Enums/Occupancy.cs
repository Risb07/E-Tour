namespace eTour.Domain.Enums;

/// <summary>
/// BRD 3.7 room options, chosen per passenger and scoped by passenger type: an adult can
/// never be booked into a child category and vice versa (see <see cref="OccupancyExtensions.AppliesTo"/>).
///
/// Maps onto the TourCost columns as:
///   TWIN               -> BasePrice           (2 sharing a room, the standard fare)
///   SINGLE             -> SinglePersonCost     (room to yourself, usually a supplement)
///   TRIPLE             -> ExtraPersonCost      (3rd adult on an extra bed in a twin room)
///   EXTRA_BED          -> ExtraPersonCost      (additional adult sharing, same rate)
///   CHILD_WITH_BED     -> ChildWithBedCost     (child occupying their own bed)
///   CHILD_WITHOUT_BED  -> ChildWithoutBedCost  (child sharing an adult's bed)
///
/// TRIPLE is retained even though the booking form now offers only Twin / Single / Extra
/// person: existing passenger rows and admin room-charge rows still reference it, and it
/// prices identically to EXTRA_BED. Infants carry no occupancy at all.
/// </summary>
public enum Occupancy
{
    TWIN,
    SINGLE,
    TRIPLE,
    EXTRA_BED,
    CHILD_WITH_BED,
    CHILD_WITHOUT_BED
}

public static class OccupancyExtensions
{
    /// <summary>Adult room options, in the order the booking form presents them.</summary>
    public static readonly IReadOnlyList<Occupancy> AdultOptions = [Occupancy.TWIN, Occupancy.SINGLE, Occupancy.EXTRA_BED];

    /// <summary>Child options, in the order the booking form presents them.</summary>
    public static readonly IReadOnlyList<Occupancy> ChildOptions = [Occupancy.CHILD_WITH_BED, Occupancy.CHILD_WITHOUT_BED];

    public static bool IsChildOption(this Occupancy occupancy) =>
        occupancy is Occupancy.CHILD_WITH_BED or Occupancy.CHILD_WITHOUT_BED;

    public static bool IsAdultOption(this Occupancy occupancy) => !occupancy.IsChildOption();

    /// <summary>
    /// Whether this option may be selected for the given passenger type. This is the single
    /// rule behind "adult options never appear for a child, child options never appear for an
    /// adult", enforced server-side so a hand-crafted request cannot slip an invalid pairing past the UI.
    /// </summary>
    public static bool AppliesTo(this Occupancy occupancy, PassengerType? type)
    {
        if (type is null)
        {
            return true; // unspecified type - legacy request, nothing to contradict
        }

        return type.Value switch
        {
            PassengerType.ADULT => occupancy.IsAdultOption(),
            PassengerType.CHILD => occupancy.IsChildOption(),
            PassengerType.INFANT => false, // infants are free and bedless
            _ => true
        };
    }

    /// <summary>The option a passenger of this type gets when none was chosen. Null for infants.</summary>
    public static Occupancy? DefaultFor(PassengerType? type, bool? needsExtraBed)
    {
        if (type is null)
        {
            return Occupancy.TWIN;
        }

        return type.Value switch
        {
            PassengerType.ADULT => Occupancy.TWIN,
            // Falls back to the legacy per-passenger flag so a request that still sends
            // needsExtraBed instead of a child category prices exactly as it did before.
            PassengerType.CHILD => (needsExtraBed is null || needsExtraBed.Value)
                ? Occupancy.CHILD_WITH_BED
                : Occupancy.CHILD_WITHOUT_BED,
            PassengerType.INFANT => null,
            _ => Occupancy.TWIN
        };
    }

    public static string GetLabel(this Occupancy occupancy) => occupancy switch
    {
        Occupancy.TWIN => "Twin sharing",
        Occupancy.SINGLE => "Single occupancy",
        Occupancy.TRIPLE => "Triple occupancy",
        Occupancy.EXTRA_BED => "Extra person",
        Occupancy.CHILD_WITH_BED => "Child with bed",
        Occupancy.CHILD_WITHOUT_BED => "Child without bed",
        _ => occupancy.ToString()
    };
}
