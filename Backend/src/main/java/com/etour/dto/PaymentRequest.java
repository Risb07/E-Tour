package com.etour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod; // CARD / UPI / NETBANKING / WALLET (simulated - no real gateway)

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
