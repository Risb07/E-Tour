package com.etour.service;

import java.util.List;

import com.etour.entity.Booking;

public interface BookingService {

	public List<Booking> getAllBooking();
	
	public Booking insertBooking(Booking booking);
	
	public Booking editBooking( long booking_id,  long customer_id,  long schedule_id, Booking bookingDetails);
	
	public void deleteBooking( long booking_id,  long customer_id,  long schedule_id);
	
}
