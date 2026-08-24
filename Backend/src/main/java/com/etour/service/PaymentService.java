package com.etour.service;

import com.etour.dto.CardPaymentRequest;
import com.etour.dto.PaymentRequest;
import com.etour.dto.PaymentResponse;
import com.etour.dto.PaymentSummaryResponse;

public interface PaymentService {
    // Records a payment for the authenticated customer's own booking, marks
    // the booking CONFIRMED, and auto-generates the invoice + receipt e-mail.
    PaymentResponse recordPayment(PaymentRequest request);

    /**
     * BRD 3.7 card payment. Shares the whole confirmation path with
     * recordPayment - only the gateway call differs.
     */
    PaymentResponse payWithCard(CardPaymentRequest request);

    /** Booking + money breakdown shown on the payment page before charging. */
    PaymentSummaryResponse getPaymentSummary(Long bookingId);

    PaymentResponse getPayment(Long paymentId);
}
