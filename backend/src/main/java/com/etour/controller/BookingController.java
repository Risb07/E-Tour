package com.etour.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.entity.Booking;
import com.etour.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

private final BookingService bookingService;
	
	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}
	
	@GetMapping
	public List<Booking> getAllBooking(){
		return bookingService.getAllBooking();
	}
	
	@PostMapping
	public Booking insertBooking(@RequestBody Booking booking) {
		return bookingService.insertBooking(booking);
	}
	
	@PutMapping("/{booking_id}/customer/{customer_id}/schedule/{schedule_id}")
	public Booking editBooking(@PathVariable("booking_id") long booking_id, 
			@PathVariable("customer_id") long customer_id, 
			@PathVariable("schedule_id") long schedule_id, 
			@RequestBody Booking booking) {
		return bookingService.editBooking(booking_id, customer_id, schedule_id, booking);
	}
	
	@DeleteMapping("/{booking_id}/customer/{customer_id}/schedule/{schedule_id}")
	public void deleteBooking(@PathVariable("booking_id") long booking_id, 
			@PathVariable("customer_id") long customer_id, 
			@PathVariable("schedule_id") long schedule_id) {
		bookingService.deleteBooking(booking_id, customer_id, schedule_id);
	}
	
}
