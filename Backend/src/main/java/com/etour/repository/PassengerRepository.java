package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByBooking_BookingId(Long bookingId);
    Optional<Passenger> findByPassengerIdAndBooking_BookingId(Long passengerId, Long bookingId);
}
