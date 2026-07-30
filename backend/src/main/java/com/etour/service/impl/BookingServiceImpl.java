package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.entity.Booking;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.BookingRepository;
import com.etour.service.BookingService;

@Service
public class BookingServiceImpl implements BookingService {

private final BookingRepository bookingRepository;
	
	public BookingServiceImpl(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}
	
	public List<Booking> getAllBooking(){
		return bookingRepository.findAll();
	}

	public Booking insertBooking(Booking booking) {
		return bookingRepository.save(booking);
	}
	
	public Booking editBooking(long booking_id, 
			long customer_id, 
			long schedule_id, 
			Booking bookingDetails) {
		Booking booking = bookingRepository.findByBookingIdAndCustomerIdAndScheduleId(
				booking_id, customer_id, schedule_id);
		if (booking == null) 
	        throw new ResourceNotFoundException("Booking not found");
		booking.setBookingStatus(bookingDetails.getBookingStatus());
		booking.setTotalAmount(bookingDetails.getTotalAmount());
		return bookingRepository.save(booking);
	}
	
	public void deleteBooking(long booking_id, long customer_id, long schedule_id){
		Booking booking = bookingRepository.findByBookingIdAndCustomerIdAndScheduleId(booking_id, customer_id, schedule_id);

		if (booking == null) 
	        throw new ResourceNotFoundException("Booking not found");

		bookingRepository.delete(booking);
	}
	
}
