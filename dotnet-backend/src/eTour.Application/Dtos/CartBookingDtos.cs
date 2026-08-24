namespace eTour.Application.Dtos;

public class BookingAddonSelection
{
    public long AddonId { get; set; }
    public int Quantity { get; set; } = 1;
}

public class BookingRequest
{
    public long ScheduleId { get; set; }
    public int NumberOfPassengers { get; set; }
    public int? AdultCount { get; set; }
    public int? ChildCount { get; set; }
    public List<PassengerInput>? Passengers { get; set; }
    public List<BookingAddonSelection> Addons { get; set; } = [];
}

public class BookingAddonResponse
{
    public BookingAddonResponse()
    {
    }

    public BookingAddonResponse(long bookingAddonId, long addonId, string addonName, string priceType,
        decimal unitPrice, int quantity, decimal totalAddonCost)
    {
        BookingAddonId = bookingAddonId;
        AddonId = addonId;
        AddonName = addonName;
        PriceType = priceType;
        UnitPrice = unitPrice;
        Quantity = quantity;
        TotalAddonCost = totalAddonCost;
    }

    public long BookingAddonId { get; set; }
    public long AddonId { get; set; }
    public string AddonName { get; set; } = null!;
    public string PriceType { get; set; } = null!;
    public decimal UnitPrice { get; set; }
    public int Quantity { get; set; }
    public decimal TotalAddonCost { get; set; }
}

public class BookingResponse
{
    public BookingResponse()
    {
    }

    public BookingResponse(long bookingId, long customerId, string customerName, long scheduleId, long tourId,
        string tourTitle, DateOnly departureDate, DateOnly bookingDate, decimal totalAmount, string? orderNumber,
        string bookingStatus, List<BookingAddonResponse> addons)
    {
        BookingId = bookingId;
        CustomerId = customerId;
        CustomerName = customerName;
        ScheduleId = scheduleId;
        TourId = tourId;
        TourTitle = tourTitle;
        DepartureDate = departureDate;
        BookingDate = bookingDate;
        TotalAmount = totalAmount;
        OrderNumber = orderNumber;
        BookingStatus = bookingStatus;
        Addons = addons;
    }

    public long BookingId { get; set; }
    public long CustomerId { get; set; }
    public string CustomerName { get; set; } = null!;
    public long ScheduleId { get; set; }
    public long TourId { get; set; }
    public string TourTitle { get; set; } = null!;
    public DateOnly DepartureDate { get; set; }
    public DateOnly BookingDate { get; set; }
    public decimal TotalAmount { get; set; }
    public string? OrderNumber { get; set; }
    public string BookingStatus { get; set; } = null!;
    public List<BookingAddonResponse> Addons { get; set; } = [];

    public int? NumberOfPassengers { get; set; }
    public int? AdultCount { get; set; }
    public int? ChildCount { get; set; }
    public bool PassengersFinalized { get; set; }
    public RoomSummary? RoomSummary { get; set; }
    public List<CostBreakdownLine>? Breakdown { get; set; }
    public List<PassengerPriceLine>? PassengerLines { get; set; }
}

public class BookingQuoteResponse
{
    public BookingQuoteResponse()
    {
    }

    public BookingQuoteResponse(long scheduleId, RoomSummary roomSummary, List<CostBreakdownLine> breakdown,
        decimal passengersTotal, decimal addonsTotal, decimal totalAmount)
    {
        ScheduleId = scheduleId;
        RoomSummary = roomSummary;
        Breakdown = breakdown;
        PassengersTotal = passengersTotal;
        AddonsTotal = addonsTotal;
        TotalAmount = totalAmount;
    }

    public long ScheduleId { get; set; }
    public RoomSummary RoomSummary { get; set; } = null!;
    public List<CostBreakdownLine> Breakdown { get; set; } = [];
    public decimal PassengersTotal { get; set; }
    public decimal AddonsTotal { get; set; }
    /// <summary>TotalAmount, not Total - the review screen's headline figure is read by this name.</summary>
    public decimal TotalAmount { get; set; }
    public List<PassengerPriceLine> PassengerLines { get; set; } = [];
}

public class CartAddonSelection
{
    public long AddonId { get; set; }
    public int Quantity { get; set; } = 1;
}

public class CartRequest
{
    public long ScheduleId { get; set; }
    public int AdultCount { get; set; }
    public int ChildCount { get; set; }
    public List<CartAddonSelection> Addons { get; set; } = [];
}

public class CartAddonResponse
{
    public long CartAddonId { get; set; }
    public long AddonId { get; set; }
    public string AddonName { get; set; } = null!;
    public int Quantity { get; set; }
    public decimal EstimatedCost { get; set; }
}

public class CartResponse
{
    public long CartId { get; set; }
    public long ScheduleId { get; set; }
    public long TourId { get; set; }
    public string TourTitle { get; set; } = null!;
    public DateOnly DepartureDate { get; set; }
    public string? PaxSummary { get; set; }
    public int? AdultCount { get; set; }
    public int? ChildCount { get; set; }
    public decimal EstimatedAmount { get; set; }
    public string Status { get; set; } = null!;
    public DateTime CreatedAt { get; set; }
    public List<CartAddonResponse> Addons { get; set; } = [];
}
