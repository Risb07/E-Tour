package com.etour.service;

import java.util.List;

import com.etour.dto.BookingQuoteResponse;
import com.etour.dto.BookingRequest;
import com.etour.dto.BookingResponse;
import com.etour.dto.PassengerInput;
import com.etour.enums.BookingStatus;

public interface BookingService {

    // Creates a booking for the CURRENTLY AUTHENTICATED customer.
    BookingResponse createBooking(BookingRequest request);

    // Read-only price preview (BRD 3.7 "Done" summary) - nothing is persisted.
    BookingQuoteResponse quote(BookingRequest request);

    // Attaches passenger details to a booking that was created without them
    // (cart checkout) and recalculates its total using banded pricing.
    // Only allowed while the booking has zero passengers so far.
    BookingResponse finalizePassengers(Long bookingId, List<PassengerInput> passengers);

    BookingResponse getBookingById(Long bookingId);

    // Admin: all bookings. Customer: only their own (enforced in impl).
    List<BookingResponse> getAllBookings();

    List<BookingResponse> getMyBookings();

    BookingResponse updateStatus(Long bookingId, BookingStatus status);

    void cancelBooking(Long bookingId);
}
