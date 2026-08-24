package com.etour.payment;

import java.math.BigDecimal;

/**
 * Boundary between "our booking/payment business logic" and "whoever actually
 * moves the money". PaymentServiceImpl owns the domain rules (ownership
 * checks, status transitions, invoice + receipt); this interface owns only the
 * charge itself.
 *
 * To plug in a real provider, add a new implementation and mark it
 * {@code @Primary} (or select it by an {@code app.payment.provider} property).
 * No business logic should need to change.
 *
 * A real gateway is usually asynchronous: you create a PENDING payment, redirect
 * the user, then settle from a webhook. That flow fits here too - {@link #charge}
 * returns PENDING and the webhook controller updates the Payment row later.
 */
public interface PaymentGateway {

    /** Provider key for logging / persistence, e.g. "SIMULATED", "RAZORPAY". */
    String getProviderName();

    /**
     * Attempts to take payment. Implementations must not throw for an ordinary
     * decline - return a failed {@link GatewayResult} instead, so the caller
     * can record the attempt. Exceptions are for genuinely exceptional cases
     * (provider unreachable, misconfiguration).
     */
    GatewayResult charge(GatewayChargeRequest request);

    /** Immutable request handed to the gateway. */
    class GatewayChargeRequest {
        private final Long bookingId;
        private final BigDecimal amount;
        private final String paymentMethod;
        private final String customerEmail;
        /** Present for card payments, null for other methods. Never persisted. */
        private final CardDetails card;

        public GatewayChargeRequest(Long bookingId, BigDecimal amount, String paymentMethod, String customerEmail) {
            this(bookingId, amount, paymentMethod, customerEmail, null);
        }

        public GatewayChargeRequest(Long bookingId, BigDecimal amount, String paymentMethod,
                String customerEmail, CardDetails card) {
            this.bookingId = bookingId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.customerEmail = customerEmail;
            this.card = card;
        }

        public Long getBookingId() { return bookingId; }
        public BigDecimal getAmount() { return amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getCustomerEmail() { return customerEmail; }
        public CardDetails getCard() { return card; }
    }
}
