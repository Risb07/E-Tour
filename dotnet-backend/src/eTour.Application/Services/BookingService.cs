using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Application.Pricing;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class BookingService : IBookingService
{
    private readonly IBookingLockRepository _bookingLockRepository;
    private readonly ITourScheduleLockRepository _scheduleLockRepository;
    private readonly IGenericRepository<Booking, long> _bookingRepository;
    private readonly IGenericRepository<TourSchedule, long> _scheduleRepository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<TourAddon, long> _addonRepository;
    private readonly IGenericRepository<BookingAddon, long> _bookingAddonRepository;
    private readonly IGenericRepository<Passenger, long> _passengerRepository;
    private readonly IGenericRepository<TourCost, long> _tourCostRepository;
    private readonly IGenericRepository<RoomCharge, long> _roomChargeRepository;
    private readonly TourPricingCalculator _pricingCalculator;
    private readonly ICurrentUserService _currentUser;

    public BookingService(IBookingLockRepository bookingLockRepository, ITourScheduleLockRepository scheduleLockRepository,
        IGenericRepository<Booking, long> bookingRepository, IGenericRepository<TourSchedule, long> scheduleRepository,
        IGenericRepository<Tour, long> tourRepository,
        IGenericRepository<TourAddon, long> addonRepository, IGenericRepository<BookingAddon, long> bookingAddonRepository,
        IGenericRepository<Passenger, long> passengerRepository, IGenericRepository<TourCost, long> tourCostRepository,
        IGenericRepository<RoomCharge, long> roomChargeRepository, TourPricingCalculator pricingCalculator,
        ICurrentUserService currentUser)
    {
        _bookingLockRepository = bookingLockRepository;
        _scheduleLockRepository = scheduleLockRepository;
        _bookingRepository = bookingRepository;
        _scheduleRepository = scheduleRepository;
        _tourRepository = tourRepository;
        _addonRepository = addonRepository;
        _bookingAddonRepository = bookingAddonRepository;
        _passengerRepository = passengerRepository;
        _tourCostRepository = tourCostRepository;
        _roomChargeRepository = roomChargeRepository;
        _pricingCalculator = pricingCalculator;
        _currentUser = currentUser;
    }

    public async Task<BookingResponse> CreateBookingAsync(BookingRequest request, CancellationToken ct = default)
    {
        // Customer is NEVER taken from the request body - always the authenticated principal.
        var customer = await _currentUser.CurrentCustomerAsync(ct);

        return await _scheduleLockRepository.WithLockAsync(request.ScheduleId, async schedule =>
        {
            if (schedule is null)
            {
                throw new ResourceNotFoundException("Tour schedule not found");
            }

            if (schedule.AvailableSeats < request.NumberOfPassengers)
            {
                throw new IllegalOperationException($"Only {schedule.AvailableSeats} seat(s) left on this schedule");
            }

            var passengersProvided = request.Passengers is { Count: > 0 };
            if (passengersProvided && request.Passengers!.Count != request.NumberOfPassengers)
            {
                throw new IllegalOperationException(
                    $"Passenger count ({request.Passengers!.Count}) does not match numberOfPassengers ({request.NumberOfPassengers})");
            }

            ValidateDeclaredComposition(request.AdultCount, request.ChildCount, request.NumberOfPassengers);
            if (passengersProvided)
            {
                ValidatePassengerComposition(request.Passengers!, schedule.DepartureDate, request.AdultCount);
            }

            var addonsTotal = 0m;
            var addonCatalog = new List<TourAddon>();
            foreach (var sel in request.Addons)
            {
                var catalog = _addonRepository.Query()
                    .FirstOrDefault(a => a.AddonId == sel.AddonId && a.TourId == schedule.TourId && a.Status)
                    ?? throw new ResourceNotFoundException($"Add-on not found: {sel.AddonId}");
                addonCatalog.Add(catalog);
                addonsTotal += catalog.Price * sel.Quantity;
            }

            decimal passengersTotal;
            PricingResult? pricing = null;
            if (passengersProvided)
            {
                pricing = PriceFor(schedule, request.Passengers!);
                passengersTotal = pricing.PassengersTotal;
            }
            else
            {
                // No passenger details yet (e.g. cart checkout) - flat ESTIMATE, corrected once
                // passengers are finalized (FinalizePassengersAsync) and re-checked at payment time.
                passengersTotal = schedule.Price * request.NumberOfPassengers;
            }
            var total = passengersTotal + addonsTotal;

            var booking = new Booking
            {
                CustomerId = customer.CustomerId,
                ScheduleId = schedule.ScheduleId,
                BookingDate = DateOnly.FromDateTime(DateTime.UtcNow),
                BookingStatus = BookingStatus.PENDING,
                NumberOfPassengers = request.NumberOfPassengers,
                AdultCount = request.AdultCount,
                ChildCount = request.ChildCount,
                TotalAmount = total
            };
            var saved = await _bookingRepository.AddAsync(booking, ct);

            var addonRows = new List<BookingAddon>();
            for (var i = 0; i < request.Addons.Count; i++)
            {
                var sel = request.Addons[i];
                var catalog = addonCatalog[i];
                addonRows.Add(new BookingAddon
                {
                    BookingId = saved.BookingId,
                    AddonId = catalog.AddonId,
                    AddonName = catalog.AddonName,
                    PriceType = catalog.PriceType,
                    UnitPrice = catalog.Price,
                    Quantity = sel.Quantity,
                    TotalAddonCost = catalog.Price * sel.Quantity
                });
            }
            foreach (var row in addonRows)
            {
                await _bookingAddonRepository.AddAsync(row, ct);
            }

            var passengerRows = new List<Passenger>();
            if (passengersProvided)
            {
                passengerRows = BuildPassengers(saved.BookingId, request.Passengers!, pricing);
                foreach (var row in passengerRows)
                {
                    await _passengerRepository.AddAsync(row, ct);
                }
            }

            // Hold the seats.
            schedule.AvailableSeats -= request.NumberOfPassengers;

            var tourTitle = GetTourTitle(schedule.TourId);
            return ToResponse(saved, customer, schedule, tourTitle, addonRows, passengerRows, pricing);
        }, ct);
    }

    public Task<BookingQuoteResponse> QuoteAsync(BookingRequest request, CancellationToken ct = default)
    {
        var schedule = _scheduleRepository.Query().FirstOrDefault(s => s.ScheduleId == request.ScheduleId)
            ?? throw new ResourceNotFoundException("Tour schedule not found");

        if (request.Passengers is null || request.Passengers.Count != request.NumberOfPassengers)
        {
            throw new IllegalOperationException("Provide one passenger entry (with date of birth) per seat to quote a price");
        }

        ValidateDeclaredComposition(request.AdultCount, request.ChildCount, request.NumberOfPassengers);
        ValidatePassengerComposition(request.Passengers, schedule.DepartureDate, request.AdultCount);

        var addonsTotal = 0m;
        foreach (var sel in request.Addons)
        {
            var catalog = _addonRepository.Query()
                .FirstOrDefault(a => a.AddonId == sel.AddonId && a.TourId == schedule.TourId && a.Status)
                ?? throw new ResourceNotFoundException($"Add-on not found: {sel.AddonId}");
            addonsTotal += catalog.Price * sel.Quantity;
        }

        var pricing = PriceFor(schedule, request.Passengers);
        var total = pricing.PassengersTotal + addonsTotal;

        return Task.FromResult(new BookingQuoteResponse(schedule.ScheduleId, pricing.RoomSummary, pricing.Breakdown,
            pricing.PassengersTotal, addonsTotal, total)
        {
            PassengerLines = pricing.PassengerLines()
        });
    }

    public async Task<BookingResponse> FinalizePassengersAsync(long bookingId, List<PassengerInput> passengers, CancellationToken ct = default)
    {
        var booking = await RequireOwnedOrAdminAsync(bookingId, ct);

        if (booking.BookingStatus != BookingStatus.PENDING)
        {
            throw new IllegalOperationException("Only a pending booking can have passenger details added");
        }
        var existing = _passengerRepository.Query().Where(p => p.BookingId == bookingId).ToList();
        if (existing.Count != 0)
        {
            throw new IllegalOperationException("Passenger details have already been added to this booking");
        }
        if (passengers.Count != booking.NumberOfPassengers)
        {
            throw new IllegalOperationException($"Expected {booking.NumberOfPassengers} passenger(s), got {passengers.Count}");
        }

        var schedule = _scheduleRepository.Query().First(s => s.ScheduleId == booking.ScheduleId);
        ValidatePassengerComposition(passengers, schedule.DepartureDate, booking.AdultCount);

        var pricing = PriceFor(schedule, passengers);
        var passengerRows = BuildPassengers(bookingId, passengers, pricing);
        foreach (var row in passengerRows)
        {
            await _passengerRepository.AddAsync(row, ct);
        }

        var addons = _bookingAddonRepository.Query().Where(a => a.BookingId == bookingId).ToList();
        var addonsTotal = addons.Sum(a => a.TotalAddonCost);

        booking.TotalAmount = pricing.PassengersTotal + addonsTotal;
        await _bookingRepository.UpdateAsync(booking, ct);

        var customer = _currentUser.IsAdmin
            ? await RequireCustomerForBooking(booking, ct)
            : await _currentUser.CurrentCustomerAsync(ct);

        return ToResponse(booking, customer, schedule, GetTourTitle(schedule.TourId), addons, passengerRows, pricing);
    }

    public async Task<BookingResponse> GetBookingByIdAsync(long bookingId, CancellationToken ct = default)
    {
        var booking = await RequireOwnedOrAdminAsync(bookingId, ct);
        var schedule = _scheduleRepository.Query().First(s => s.ScheduleId == booking.ScheduleId);
        var customer = await RequireCustomerForBooking(booking, ct);
        var addons = _bookingAddonRepository.Query().Where(a => a.BookingId == bookingId).ToList();
        var passengers = _passengerRepository.Query().Where(p => p.BookingId == bookingId).ToList();
        return ToResponse(booking, customer, schedule, GetTourTitle(schedule.TourId), addons, passengers, null);
    }

    public async Task<IReadOnlyList<BookingResponse>> GetAllBookingsAsync(CancellationToken ct = default)
    {
        if (!_currentUser.IsAdmin)
        {
            throw new IllegalOperationException("Only admins can list all bookings");
        }
        var bookings = await _bookingRepository.GetAllAsync(ct);
        return bookings.Select(b => BuildResponse(b, ct)).ToList();
    }

    public async Task<IReadOnlyList<BookingResponse>> GetMyBookingsAsync(CancellationToken ct = default)
    {
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        var bookings = _bookingRepository.Query().Where(b => b.CustomerId == customer.CustomerId).ToList();
        return bookings.Select(b => BuildResponse(b, ct, customer)).ToList();
    }

    public async Task<BookingResponse> UpdateStatusAsync(long bookingId, BookingStatus status, CancellationToken ct = default)
    {
        if (!_currentUser.IsAdmin)
        {
            throw new IllegalOperationException("Only admins can change booking status directly");
        }
        var booking = await _bookingRepository.GetByIdAsync(bookingId, ct) ?? throw new ResourceNotFoundException("Booking not found");

        if (booking.BookingStatus == BookingStatus.CANCELLED && status != BookingStatus.CANCELLED)
        {
            throw new IllegalOperationException("Cannot change status of a cancelled booking");
        }

        booking.BookingStatus = status;
        await _bookingRepository.UpdateAsync(booking, ct);
        return BuildResponse(booking, ct);
    }

    public async Task CancelBookingAsync(long bookingId, CancellationToken ct = default)
    {
        await _bookingLockRepository.WithLockAsync(bookingId, async lockedBooking =>
        {
            var booking = lockedBooking ?? throw new ResourceNotFoundException("Booking not found");

            if (!_currentUser.IsAdmin)
            {
                var customer = await _currentUser.CurrentCustomerAsync(ct);
                if (booking.CustomerId != customer.CustomerId)
                {
                    throw new ResourceNotFoundException("Booking not found");
                }
            }

            if (booking.BookingStatus == BookingStatus.CANCELLED)
            {
                throw new IllegalOperationException("Booking is already cancelled");
            }

            booking.BookingStatus = BookingStatus.CANCELLED;

            // Release the seats back to the schedule using the persisted count, not derived
            // from totalAmount (which also includes add-ons).
            var passengers = booking.NumberOfPassengers ?? 1;
            var schedule = await _scheduleRepository.GetByIdAsync(booking.ScheduleId, ct);
            if (schedule is not null)
            {
                schedule.AvailableSeats += passengers;
                await _scheduleRepository.UpdateAsync(schedule, ct);
            }

            return true;
        }, ct);
    }

    // ---------------------------------------------------------------------

    /// <summary>
    /// A booking must have a lead adult - otherwise a party of children (or a single newborn)
    /// could be booked, and since infants are free and children discounted, the total could be
    /// zero. Two independent checks: the declared composition must be consistent and contain an
    /// adult, and the passengers' actual DOBs must produce an adult in an adult slot.
    /// </summary>
    private static void ValidateDeclaredComposition(int? adultCount, int? childCount, int numberOfPassengers)
    {
        if (adultCount is null && childCount is null)
        {
            return; // legacy request - nothing was declared
        }
        var adults = adultCount ?? 0;
        var children = childCount ?? 0;

        if (adults + children != numberOfPassengers)
        {
            throw new IllegalOperationException(
                $"Adults ({adults}) plus children ({children}) must equal the number of passengers ({numberOfPassengers})");
        }
        if (adults < 1)
        {
            throw new IllegalOperationException("At least one adult is required for every booking.");
        }
    }

    /// <summary>
    /// Slots are positional, matching the passenger form: the first `adultCount` entries are
    /// adult slots, the rest are child slots.
    /// </summary>
    private static void ValidatePassengerComposition(List<PassengerInput> passengers, DateOnly departureDate, int? adultCount)
    {
        var hasAdult = false;
        for (var i = 0; i < passengers.Count; i++)
        {
            var band = PassengerTypeExtensions.FromAge(passengers[i].Dob, departureDate);
            if (band == PassengerType.ADULT)
            {
                hasAdult = true;
            }

            if (adultCount is null)
            {
                continue;
            }

            var isAdultSlot = i < adultCount;
            if (isAdultSlot && band != PassengerType.ADULT)
            {
                throw new IllegalOperationException(
                    $"Passenger {i + 1} was booked as an adult but the date of birth given is a {band.GetLabel().ToLower()} on the departure date");
            }
            if (!isAdultSlot && band == PassengerType.ADULT)
            {
                throw new IllegalOperationException(
                    $"Passenger {i + 1} was booked as a child but the date of birth given is an adult on the departure date");
            }
        }

        if (!hasAdult)
        {
            throw new IllegalOperationException("At least one adult is required for every booking.");
        }
    }

    private PricingResult PriceFor(TourSchedule schedule, List<PassengerInput> passengers)
    {
        var cost = _tourCostRepository.Query()
            .Where(c => c.TourId == schedule.TourId && c.Status == 1 && c.ValidFrom <= schedule.DepartureDate && c.ValidTo >= schedule.DepartureDate)
            .OrderByDescending(c => c.CostId)
            .FirstOrDefault();
        return _pricingCalculator.Calculate(passengers, schedule.DepartureDate, cost, schedule.Price, RoomChargesFor(schedule.TourId));
    }

    private Dictionary<Occupancy, decimal> RoomChargesFor(long tourId) =>
        _roomChargeRepository.Query().Where(rc => rc.TourId == tourId && rc.Active)
            .ToDictionary(rc => rc.Occupancy, rc => rc.Charge);

    private static List<Passenger> BuildPassengers(long bookingId, List<PassengerInput> inputs, PricingResult? pricing)
    {
        var rows = new List<Passenger>(inputs.Count);
        for (var i = 0; i < inputs.Count; i++)
        {
            var p = inputs[i];
            var passenger = new Passenger
            {
                BookingId = bookingId,
                FullName = p.FullName,
                Gender = p.Gender,
                Dob = p.Dob,
                Nationality = p.Nationality,
                IdProofType = p.IdProofType,
                IdProofNumber = p.IdProofNumber,
                // Passed through as-is (not coerced to true) - matches Java's
                // passenger.setNeedsExtraBed(p.getNeedsExtraBed()), which stores null when the
                // client didn't specify it rather than defaulting the stored value.
                NeedsExtraBed = p.NeedsExtraBed,
                AddressLine1 = p.AddressLine1,
                AddressLine2 = p.AddressLine2,
                City = p.City,
                State = p.State,
                Country = p.Country,
                Pincode = p.Pincode
            };

            var resolved = pricing is not null && i < pricing.Passengers.Count ? pricing.Passengers[i] : null;
            if (resolved is not null)
            {
                passenger.PassengerType = resolved.Type;
                passenger.Occupancy = resolved.Occupancy;
                passenger.RoomCharge = resolved.Occupancy is null ? null : resolved.RoomCharge;
                passenger.PassengerPrice = resolved.Price;
            }
            else
            {
                passenger.Occupancy = p.Occupancy;
            }
            rows.Add(passenger);
        }
        return rows;
    }

    private async Task<Booking> RequireOwnedOrAdminAsync(long bookingId, CancellationToken ct)
    {
        var booking = await _bookingRepository.GetByIdAsync(bookingId, ct) ?? throw new ResourceNotFoundException("Booking not found");

        if (_currentUser.IsAdmin)
        {
            return booking;
        }

        var customer = await _currentUser.CurrentCustomerAsync(ct);
        if (booking.CustomerId != customer.CustomerId)
        {
            // 404 instead of 403 on purpose - don't confirm the booking exists to a non-owner.
            throw new ResourceNotFoundException("Booking not found");
        }
        return booking;
    }

    private async Task<Customer> RequireCustomerForBooking(Booking booking, CancellationToken ct)
    {
        // Used for admin-viewed bookings, where the current principal isn't the booking's owner.
        if (!_currentUser.IsAdmin)
        {
            return await _currentUser.CurrentCustomerAsync(ct);
        }
        return _bookingRepository.Query().Where(b => b.BookingId == booking.BookingId).Select(b => b.Customer).First();
    }

    /// <summary>Query() never eager-loads navigations, so the title is fetched via its own projection rather than schedule.Tour.Title.</summary>
    private string GetTourTitle(long tourId) =>
        _tourRepository.Query().Where(t => t.TourId == tourId).Select(t => t.Title).FirstOrDefault() ?? "";

    private BookingResponse BuildResponse(Booking booking, CancellationToken ct, Customer? knownCustomer = null)
    {
        var schedule = _scheduleRepository.Query().First(s => s.ScheduleId == booking.ScheduleId);
        var customer = knownCustomer ?? _bookingRepository.Query().Where(b => b.BookingId == booking.BookingId).Select(b => b.Customer).First();
        var addons = _bookingAddonRepository.Query().Where(a => a.BookingId == booking.BookingId).ToList();
        var passengers = _passengerRepository.Query().Where(p => p.BookingId == booking.BookingId).ToList();
        return ToResponse(booking, customer, schedule, GetTourTitle(schedule.TourId), addons, passengers, null);
    }

    private static BookingResponse ToResponse(Booking booking, Customer customer, TourSchedule schedule, string tourTitle,
        List<BookingAddon> addons, List<Passenger> passengers, PricingResult? pricing)
    {
        var addonDtos = addons.Select(a => new BookingAddonResponse(a.BookingAddonId, a.AddonId, a.AddonName,
            a.PriceType.ToString(), a.UnitPrice, a.Quantity, a.TotalAddonCost)).ToList();

        var response = new BookingResponse(booking.BookingId, customer.CustomerId, customer.FullName, schedule.ScheduleId,
            schedule.TourId, tourTitle, schedule.DepartureDate, booking.BookingDate, booking.TotalAmount,
            booking.OrderNumber, booking.BookingStatus.ToString(), addonDtos)
        {
            NumberOfPassengers = booking.NumberOfPassengers,
            AdultCount = booking.AdultCount,
            ChildCount = booking.ChildCount,
            PassengersFinalized = passengers.Count != 0
        };

        if (pricing is not null)
        {
            response.RoomSummary = pricing.RoomSummary;
            response.Breakdown = pricing.Breakdown;
            response.PassengerLines = pricing.PassengerLines();
        }

        return response;
    }
}
