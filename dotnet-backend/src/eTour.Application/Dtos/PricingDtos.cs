using eTour.Domain.Enums;

namespace eTour.Application.Dtos;

/// <summary>One passenger as submitted by the client - DOB is the source of truth for the passenger band, not any type the client sends.</summary>
public class PassengerInput
{
    public string FullName { get; set; } = null!;
    public string? Gender { get; set; }
    public DateOnly? Dob { get; set; }
    public string? Nationality { get; set; }
    public string? IdProofType { get; set; }
    public string? IdProofNumber { get; set; }
    public bool? NeedsExtraBed { get; set; }
    public Occupancy? Occupancy { get; set; }
    public string? AddressLine1 { get; set; }
    public string? AddressLine2 { get; set; }
    public string? City { get; set; }
    public string? State { get; set; }
    public string? Country { get; set; }
    public string? Pincode { get; set; }
}

/// <summary>
/// What one named passenger costs, and why. The type/occupancy here are the RESOLVED values the
/// server actually priced on, not the raw request values.
///
/// Each enum is sent twice on purpose, matching Java: the raw name for logic, and a human label
/// for display. The booking summary renders the labels directly and only falls back to its own
/// client-side guess when they are absent - so dropping them silently downgrades the review
/// screen from server-authoritative pricing to a recomputed approximation.
/// </summary>
public class PassengerPriceLine
{
    /// <summary>Position in the submitted passenger list, so a client can match rows up.</summary>
    public int PassengerIndex { get; set; }
    public string FullName { get; set; } = null!;

    public string PassengerType { get; set; } = null!;
    public string PassengerTypeLabel { get; set; } = null!;

    /// <summary>Null for infants, who occupy no bed.</summary>
    public string? Occupancy { get; set; }
    public string? OccupancyLabel { get; set; }

    /// <summary>The TourCost category rate before any room supplement.</summary>
    public decimal CategoryRate { get; set; }
    /// <summary>Admin-configured supplement for the chosen occupancy, usually zero.</summary>
    public decimal RoomCharge { get; set; }
    /// <summary>CategoryRate + RoomCharge - what this passenger contributes to the total.</summary>
    public decimal Price { get; set; }

    public PassengerPriceLine()
    {
    }

    public PassengerPriceLine(int passengerIndex, string fullName, PassengerType passengerType,
        Occupancy? occupancy, decimal categoryRate, decimal roomCharge, decimal price)
    {
        PassengerIndex = passengerIndex;
        FullName = fullName;
        PassengerType = passengerType.ToString();
        PassengerTypeLabel = passengerType.GetLabel();
        Occupancy = occupancy?.ToString();
        OccupancyLabel = occupancy?.GetLabel();
        CategoryRate = categoryRate;
        RoomCharge = roomCharge;
        Price = price;
    }
}

public class CostBreakdownLine
{
    public CostBreakdownLine()
    {
    }

    public CostBreakdownLine(string label, decimal unitPrice, int quantity, decimal lineTotal)
    {
        Label = label;
        UnitPrice = unitPrice;
        Quantity = quantity;
        LineTotal = lineTotal;
    }

    public string Label { get; set; } = null!;
    public decimal UnitPrice { get; set; }
    public int Quantity { get; set; }
    /// <summary>Named LineTotal (not Total) to match Java - the cost-breakdown rows read it by this name.</summary>
    public decimal LineTotal { get; set; }
}

public class RoomSummary
{
    public RoomSummary()
    {
    }

    public RoomSummary(int adults, int children, int infants, int doubleRooms, int singleRooms, int extraBeds)
    {
        Adults = adults;
        Children = children;
        Infants = infants;
        DoubleRooms = doubleRooms;
        SingleRooms = singleRooms;
        ExtraBeds = extraBeds;
    }

    public int Adults { get; set; }
    public int Children { get; set; }
    public int Infants { get; set; }
    public int DoubleRooms { get; set; }
    // SingleRooms/ExtraBeds, not SingleAdults/ExtraBedAdults: these name the ROOM requirement the
    // summary line reports ("2 twin, 1 single"), which is what the client renders by these names.
    public int SingleRooms { get; set; }
    public int ExtraBeds { get; set; }
}
