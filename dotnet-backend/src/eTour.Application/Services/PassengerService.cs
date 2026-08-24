using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;

namespace eTour.Application.Services;

public class PassengerService : IPassengerService
{
    private readonly IGenericRepository<Passenger, long> _passengerRepository;
    private readonly IGenericRepository<Booking, long> _bookingRepository;
    private readonly ICurrentUserService _currentUser;

    public PassengerService(IGenericRepository<Passenger, long> passengerRepository, IGenericRepository<Booking, long> bookingRepository,
        ICurrentUserService currentUser)
    {
        _passengerRepository = passengerRepository;
        _bookingRepository = bookingRepository;
        _currentUser = currentUser;
    }

    public async Task<PassengerDto> AddPassengerAsync(PassengerDto dto, CancellationToken ct = default)
    {
        var booking = await RequireOwnedBookingAsync(dto.BookingId, ct);

        var passenger = new Passenger { BookingId = booking.BookingId };
        ApplyDto(dto, passenger, DepartureDateOf(booking.BookingId));

        var saved = await _passengerRepository.AddAsync(passenger, ct);
        return ToDto(saved);
    }

    public async Task<PassengerDto> UpdatePassengerAsync(long passengerId, PassengerDto dto, CancellationToken ct = default)
    {
        var passenger = await _passengerRepository.GetByIdAsync(passengerId, ct) ?? throw new ResourceNotFoundException("Passenger not found");

        // Re-validate ownership via the passenger's own booking, not the (client-controlled) bookingId in the request body.
        var booking = await RequireOwnedBookingAsync(passenger.BookingId, ct);

        ApplyDto(dto, passenger, DepartureDateOf(booking.BookingId));
        await _passengerRepository.UpdateAsync(passenger, ct);
        return ToDto(passenger);
    }

    /// <summary>Query() never eager-loads navigations, so this is fetched via its own projection rather than booking.Schedule.DepartureDate.</summary>
    private DateOnly? DepartureDateOf(long bookingId) =>
        _bookingRepository.Query().Where(b => b.BookingId == bookingId).Select(b => (DateOnly?)b.Schedule.DepartureDate).FirstOrDefault();

    public async Task<IReadOnlyList<PassengerDto>> GetPassengersForBookingAsync(long bookingId, CancellationToken ct = default)
    {
        await RequireOwnedBookingAsync(bookingId, ct);
        var passengers = _passengerRepository.Query().Where(p => p.BookingId == bookingId).ToList();
        return passengers.Select(ToDto).ToList();
    }

    public async Task DeletePassengerAsync(long passengerId, CancellationToken ct = default)
    {
        var passenger = await _passengerRepository.GetByIdAsync(passengerId, ct) ?? throw new ResourceNotFoundException("Passenger not found");
        await RequireOwnedBookingAsync(passenger.BookingId, ct);
        await _passengerRepository.DeleteAsync(passenger, ct);
    }

    private async Task<Booking> RequireOwnedBookingAsync(long bookingId, CancellationToken ct)
    {
        var booking = await _bookingRepository.GetByIdAsync(bookingId, ct) ?? throw new ResourceNotFoundException("Booking not found");

        if (_currentUser.IsAdmin)
        {
            return booking;
        }
        var customer = await _currentUser.CurrentCustomerAsync(ct);
        if (booking.CustomerId != customer.CustomerId)
        {
            throw new ResourceNotFoundException("Booking not found");
        }
        return booking;
    }

    /// <summary>
    /// Only name/ID/address fields and type/occupancy come from the client. The room CHARGE and
    /// PASSENGER PRICE are set by BookingService at booking time and never rewritten by a later
    /// edit - otherwise correcting a misspelt name could silently change what was charged.
    /// </summary>
    private static void ApplyDto(PassengerDto dto, Passenger passenger, DateOnly? departureDate)
    {
        passenger.FullName = dto.FullName;
        passenger.Gender = dto.Gender;
        passenger.Dob = dto.Dob;
        passenger.Nationality = dto.Nationality;
        passenger.IdProofType = dto.IdProofType;
        passenger.IdProofNumber = dto.IdProofNumber;
        passenger.NeedsExtraBed = dto.NeedsExtraBed;

        // The band is recomputed from the (possibly just-corrected) DOB rather than read from
        // the request, so a wrong DOB fix moves the passenger into the right band automatically.
        var type = PassengerTypeExtensions.FromAge(dto.Dob, departureDate);
        Occupancy? occupancy = dto.Occupancy is null ? null : Enum.Parse<Occupancy>(dto.Occupancy, true);

        if (occupancy is not null && !occupancy.Value.AppliesTo(type))
        {
            throw new IllegalOperationException(
                $"This passenger is a {type.GetLabel().ToLower()} by date of birth and cannot use the \"{occupancy.Value.GetLabel()}\" category");
        }

        passenger.PassengerType = type;
        passenger.Occupancy = occupancy;
        passenger.AddressLine1 = dto.AddressLine1;
        passenger.AddressLine2 = dto.AddressLine2;
        passenger.City = dto.City;
        passenger.State = dto.State;
        passenger.Country = dto.Country;
        passenger.Pincode = dto.Pincode;
    }

    private static PassengerDto ToDto(Passenger p) => new()
    {
        PassengerId = p.PassengerId,
        BookingId = p.BookingId,
        FullName = p.FullName,
        Gender = p.Gender,
        Dob = p.Dob,
        Nationality = p.Nationality,
        IdProofType = p.IdProofType,
        IdProofNumber = p.IdProofNumber,
        NeedsExtraBed = p.NeedsExtraBed,
        PassengerType = p.PassengerType?.ToString(),
        Occupancy = p.Occupancy?.ToString(),
        RoomCharge = p.RoomCharge,
        PassengerPrice = p.PassengerPrice,
        AddressLine1 = p.AddressLine1,
        AddressLine2 = p.AddressLine2,
        City = p.City,
        State = p.State,
        Country = p.Country,
        Pincode = p.Pincode
    };
}
