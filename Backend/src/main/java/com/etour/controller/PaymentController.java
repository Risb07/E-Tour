package com.etour.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.CardPaymentRequest;
import com.etour.dto.PaymentRequest;
import com.etour.dto.PaymentResponse;
import com.etour.dto.PaymentSummaryResponse;
import com.etour.service.PaymentService;

import jakarta.validation.Valid;

/**
 * Payments for the authenticated customer's own bookings. Access is restricted
 * to ADMIN/CUSTOMER in SecurityConfig (/api/payments/**); ownership of the
 * specific booking is re-checked in the service on every call.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Booking + money breakdown for the payment page. Amounts are computed
     * server-side from the persisted booking, so the client can display but
     * never influence what gets charged.
     */
    @GetMapping("/booking/{bookingId}/summary")
    public ResponseEntity<PaymentSummaryResponse> getSummary(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentSummary(bookingId));
    }

    /**
     * BRD 3.7 card payment. On success this confirms the booking, generates
     * the order number and invoice, and e-mails the PDF receipt.
     *
     * The card number and CVV are used for the gateway call and then
     * discarded - only brand + last four are stored.
     */
    @PostMapping("/card")
    public ResponseEntity<PaymentResponse> payWithCard(@Valid @RequestBody CardPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.payWithCard(request));
    }

    /** Non-card payment (kept for the existing simulated flow). */
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.recordPayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
