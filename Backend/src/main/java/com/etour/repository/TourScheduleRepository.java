package com.etour.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.entity.TourSchedule;

import jakarta.persistence.LockModeType;


public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {

	List<TourSchedule> findByTourTourId(Long tourId);

	// Row-locks the schedule for the duration of the enclosing transaction so
	// a seat check-then-decrement can't race with a concurrent booking on the
	// last available seat.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from TourSchedule s where s.scheduleId = :id")
	Optional<TourSchedule> findByIdForUpdate(@Param("id") Long id);

	/**
	 * Batch-loads schedules for a whole page of search results in ONE query.
	 * Without this, rendering N results with departure dates would fire N
	 * follow-up requests (the classic N+1). Ordered so the caller can keep the
	 * first schedule it sees per tour as the "nearest" departure.
	 */
	@Query("select s from TourSchedule s where s.tour.tourId in :tourIds "
			+ "and (:fromDate is null or s.departureDate >= :fromDate) "
			+ "and (:toDate is null or s.returnDate <= :toDate) "
			+ "order by s.departureDate asc")
	List<TourSchedule> findForToursInWindow(@Param("tourIds") List<Long> tourIds,
			@Param("fromDate") LocalDate fromDate,
			@Param("toDate") LocalDate toDate);

}
