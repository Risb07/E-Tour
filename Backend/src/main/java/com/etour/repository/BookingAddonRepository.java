package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.BookingAddon;

public interface BookingAddonRepository extends JpaRepository<BookingAddon, Long> {
    List<BookingAddon> findByBooking_BookingId(Long bookingId);
}
