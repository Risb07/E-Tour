package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.dto.PassengerDto;
import com.etour.entity.Booking;
import com.etour.entity.Customer;
import com.etour.entity.Passenger;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.BookingRepository;
import com.etour.repository.PassengerRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.PassengerService;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserProvider currentUserProvider;

    public PassengerServiceImpl(PassengerRepository passengerRepository, BookingRepository bookingRepository,
            CurrentUserProvider currentUserProvider) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserProvider = currentUserProvider;
    }

    private Booking requireOwnedBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (currentUserProvider.isAdmin()) {
            return booking;
        }
        Customer customer = currentUserProvider.currentCustomer();
        if (!booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Booking not found");
        }
        return booking;
    }

    @Override
    public PassengerDto addPassenger(PassengerDto dto) {
        Booking booking = requireOwnedBooking(dto.getBookingId());

        Passenger passenger = new Passenger();
        passenger.setBooking(booking);
        applyDto(dto, passenger);

        return toDto(passengerRepository.save(passenger));
    }

    @Override
    public PassengerDto updatePassenger(Long passengerId, PassengerDto dto) {
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        // Re-validate ownership via the passenger's own booking, not the
        // (client-controlled) bookingId in the request body.
        requireOwnedBooking(passenger.getBooking().getBookingId());

        applyDto(dto, passenger);
        return toDto(passengerRepository.save(passenger));
    }

    @Override
    public List<PassengerDto> getPassengersForBooking(Long bookingId) {
        requireOwnedBooking(bookingId);
        return passengerRepository.findByBooking_BookingId(bookingId).stream().map(this::toDto).toList();
    }

    @Override
    public void deletePassenger(Long passengerId) {
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
        requireOwnedBooking(passenger.getBooking().getBookingId());
        passengerRepository.delete(passenger);
    }

    private void applyDto(PassengerDto dto, Passenger passenger) {
        passenger.setFullName(dto.getFullName());
        passenger.setGender(dto.getGender());
        passenger.setDob(dto.getDob());
        passenger.setNationality(dto.getNationality());
        passenger.setIdProofType(dto.getIdProofType());
        passenger.setIdProofNumber(dto.getIdProofNumber());
        passenger.setNeedsExtraBed(dto.getNeedsExtraBed());
        // Type and occupancy only - the room CHARGE and the passenger PRICE
        // are set by BookingServiceImpl at booking time and must not be
        // rewritten by a later passenger edit, otherwise correcting a
        // misspelt name could silently change what was charged.
        //
        // The band is recomputed from the (possibly just-corrected) date of
        // birth rather than read from the request, so this path classifies by
        // exactly the same rule as the booking flow - fixing a wrong DOB here
        // moves the passenger into the right band automatically.
        com.etour.enums.PassengerType type = com.etour.enums.PassengerType.fromAge(
                dto.getDob(), departureDateOf(passenger));
        com.etour.enums.Occupancy occupancy = dto.getOccupancy() == null
                ? null
                : com.etour.enums.Occupancy.valueOf(dto.getOccupancy());

        // The same rule the booking flow enforces: an adult can never sit in a
        // child category, and vice versa.
        if (occupancy != null && !occupancy.appliesTo(type)) {
            throw new com.etour.exception.IllegalOperationException(
                    "This passenger is a " + type.getLabel().toLowerCase()
                            + " by date of birth and cannot use the \"" + occupancy.getLabel() + "\" category");
        }

        passenger.setPassengerType(type);
        passenger.setOccupancy(occupancy);
        passenger.setAddressLine1(dto.getAddressLine1());
        passenger.setAddressLine2(dto.getAddressLine2());
        passenger.setCity(dto.getCity());
        passenger.setState(dto.getState());
        passenger.setCountry(dto.getCountry());
        passenger.setPincode(dto.getPincode());
    }

    /**
     * The date the passenger's age is measured at. Null only for a passenger
     * not yet attached to a booking, in which case PassengerType.fromAge
     * falls back to treating them as an adult - the same guard the pricing
     * engine has always applied to incomplete data.
     */
    private java.time.LocalDate departureDateOf(Passenger passenger) {
        return passenger.getBooking() == null || passenger.getBooking().getSchedule() == null
                ? null
                : passenger.getBooking().getSchedule().getDepartureDate();
    }

    private PassengerDto toDto(Passenger p) {
        PassengerDto d = new PassengerDto();
        d.setPassengerId(p.getPassengerId());
        d.setBookingId(p.getBooking().getBookingId());
        d.setFullName(p.getFullName());
        d.setGender(p.getGender());
        d.setDob(p.getDob());
        d.setNationality(p.getNationality());
        d.setIdProofType(p.getIdProofType());
        d.setIdProofNumber(p.getIdProofNumber());
        d.setNeedsExtraBed(p.getNeedsExtraBed());
        d.setPassengerType(p.getPassengerType() == null ? null : p.getPassengerType().name());
        d.setOccupancy(p.getOccupancy() == null ? null : p.getOccupancy().name());
        d.setRoomCharge(p.getRoomCharge());
        d.setPassengerPrice(p.getPassengerPrice());
        d.setAddressLine1(p.getAddressLine1());
        d.setAddressLine2(p.getAddressLine2());
        d.setCity(p.getCity());
        d.setState(p.getState());
        d.setCountry(p.getCountry());
        d.setPincode(p.getPincode());
        return d;
    }
}
