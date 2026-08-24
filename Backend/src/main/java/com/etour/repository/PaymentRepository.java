package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBooking_BookingId(Long bookingId);
    Optional<Payment> findByPaymentIdAndBooking_Customer_CustomerId(Long paymentId, Long customerId);
}
