package com.etour.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.DashboardStatsResponse;
import com.etour.enums.BookingStatus;
import com.etour.enums.TourStatus;
import com.etour.repository.BookingRepository;
import com.etour.repository.CustomerRepository;
import com.etour.repository.InvoiceRepository;
import com.etour.repository.TourRepository;
import com.etour.repository.UserRepository;

// Admin-only, enforced by /api/admin/** in SecurityConfig.
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;

    public AdminDashboardController(UserRepository userRepository, CustomerRepository customerRepository,
            TourRepository tourRepository, BookingRepository bookingRepository, InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.tourRepository = tourRepository;
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalCustomers(customerRepository.count());
        stats.setTotalTours(tourRepository.count());
        stats.setActiveTours(tourRepository.findAll().stream()
                .filter(t -> t.getStatus() == TourStatus.ACTIVE).count());

        var bookings = bookingRepository.findAll();
        stats.setTotalBookings(bookings.size());
        stats.setConfirmedBookings(bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED).count());
        stats.setPendingBookings(bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PENDING).count());
        stats.setCancelledBookings(bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED).count());

        BigDecimal revenue = invoiceRepository.findAll().stream()
                .map(i -> i.getTotalAmount() == null ? BigDecimal.ZERO : i.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(revenue);

        return ResponseEntity.ok(stats);
    }
}
