namespace eTour.Domain.Enums;

/// <summary>
/// How a passenger is booked and priced. The booking form asks for it explicitly (a child
/// travelling on an adult's passport, a 12-year-old the operator has agreed to seat as an
/// adult, etc.), so the selection is the source of truth and DOB only provides the default
/// and a sanity check.
///
/// The age boundaries in <see cref="PassengerTypeExtensions.FromAge"/> are the ones the
/// pricing engine has always used, so a request that omits passenger type still bands
/// exactly as it did before.
/// </summary>
public enum PassengerType
{
    /// <summary>Age 12+ as of departure. Priced by the chosen adult occupancy.</summary>
    ADULT,

    /// <summary>Age 2-12 as of departure. Priced by child-with-bed / child-without-bed.</summary>
    CHILD,

    /// <summary>Under 2 as of departure. Free, and never occupies a room of its own.</summary>
    INFANT
}

public static class PassengerTypeExtensions
{
    private const int InfantMaxAge = 2;
    private const int ChildMaxAge = 12;

    /// <summary>
    /// The band a date of birth implies. Used as the default when the client did not send an
    /// explicit type, which is what keeps older clients (and the cart checkout path) pricing
    /// exactly as before. A null dob is treated as an adult, matching the long-standing
    /// calculator behaviour for incomplete legacy data.
    /// </summary>
    public static PassengerType FromAge(DateOnly? dob, DateOnly? departureDate)
    {
        if (dob is null || departureDate is null)
        {
            return PassengerType.ADULT;
        }

        int age = CalculateYears(dob.Value, departureDate.Value);
        if (age < InfantMaxAge)
        {
            return PassengerType.INFANT;
        }
        if (age < ChildMaxAge)
        {
            return PassengerType.CHILD;
        }
        return PassengerType.ADULT;
    }

    /// <summary>Equivalent to Java's Period.between(dob, asOf).getYears() - whole calendar years elapsed.</summary>
    private static int CalculateYears(DateOnly dob, DateOnly asOf)
    {
        int years = asOf.Year - dob.Year;
        if (asOf.Month < dob.Month || (asOf.Month == dob.Month && asOf.Day < dob.Day))
        {
            years--;
        }
        return years;
    }

    public static string GetLabel(this PassengerType type) => type switch
    {
        PassengerType.ADULT => "Adult",
        PassengerType.CHILD => "Child",
        PassengerType.INFANT => "Infant",
        _ => type.ToString()
    };
}
