package com.etour.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

	Booking findByBookingIdAndCustomerIdAndScheduleId(Long bookingId, Long customerId, Long schedule_Id);
	
}
