package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.BookingQuoteResponse;
import com.etour.dto.BookingRequest;
import com.etour.dto.BookingResponse;
import com.etour.dto.PassengerInput;
import com.etour.enums.BookingStatus;
import com.etour.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping
	public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
	}

	// BRD 3.7 "Done" pax/cost summary shown before the user hits Pay.
	// Nothing is persisted - same pricing logic used for the real booking.
	@PostMapping("/quote")
	public ResponseEntity<BookingQuoteResponse> quote(@Valid @RequestBody BookingRequest request) {
		return ResponseEntity.ok(bookingService.quote(request));
	}

	// Cart-originated bookings are created with just a headcount; this
	// attaches real passenger details and recalculates the true total
	// before payment is allowed.
	@PatchMapping("/{bookingId}/passengers")
	public ResponseEntity<BookingResponse> finalizePassengers(@PathVariable Long bookingId,
			@RequestBody List<PassengerInput> passengers) {
		return ResponseEntity.ok(bookingService.finalizePassengers(bookingId, passengers));
	}

	@GetMapping("/me")
	public ResponseEntity<List<BookingResponse>> getMyBookings() {
		return ResponseEntity.ok(bookingService.getMyBookings());
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId) {
		return ResponseEntity.ok(bookingService.getBookingById(bookingId));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<BookingResponse>> getAllBookings() {
		return ResponseEntity.ok(bookingService.getAllBookings());
	}

	@PatchMapping("/{bookingId}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<BookingResponse> updateStatus(@PathVariable Long bookingId,
			@RequestParam BookingStatus status) {
		return ResponseEntity.ok(bookingService.updateStatus(bookingId, status));
	}

	@DeleteMapping("/{bookingId}")
	public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
		bookingService.cancelBooking(bookingId);
		return ResponseEntity.noContent().build();
	}

}
