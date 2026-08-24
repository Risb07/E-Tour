package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.entity.Booking;
import com.etour.enums.BookingStatus;

import jakarta.persistence.LockModeType;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	// Ownership-safe lookup: booking must belong to this customer.
	Optional<Booking> findByBookingIdAndCustomer_CustomerId(Long bookingId, Long customerId);

	// Row-locks the booking for the duration of the enclosing transaction so
	// the "is it already CONFIRMED?" check and the charge that follows can't
	// race with a second payment attempt on the same booking. Same pattern
	// TourScheduleRepository uses to stop seats being oversold.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select b from Booking b where b.bookingId = :id")
	Optional<Booking> findByIdForUpdate(@Param("id") Long id);

	List<Booking> findByCustomer_CustomerId(Long customerId);

	/**
	 * Has this customer actually booked this tour? Used to gate review
	 * creation so only real travellers can review a tour. A cancelled
	 * booking doesn't count, so passing bookings with a CANCELLED status is
	 * excluded by the caller supplying the allowed statuses.
	 */
	boolean existsByCustomer_CustomerIdAndSchedule_Tour_TourIdAndBookingStatusIn(
			Long customerId, Long tourId, List<BookingStatus> statuses);
}
