package com.etour.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Card details for a booking payment.
 *
 * SECURITY: this object is request-scoped only. The card number and CVV are
 * used to call the gateway and are then discarded - neither is persisted or
 * logged anywhere. Only the brand and last four digits survive, on
 * Payment.cardSummary. Storing a full PAN would put the whole database in
 * PCI-DSS scope.
 *
 * With a real gateway (Razorpay/Stripe) the card would never reach this server
 * at all: the browser would tokenise it against the provider and send only the
 * token. This DTO is the seam where that swap happens - see PaymentGateway.
 */
public class CardPaymentRequest {

    @NotNull(message = "bookingId is required")
    private Long bookingId;

    /** CREDIT_CARD or DEBIT_CARD. */
    @NotBlank(message = "paymentMethod is required")
    @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD", message = "paymentMethod must be CREDIT_CARD or DEBIT_CARD")
    private String paymentMethod;

    /** Digits only - the client strips its display formatting before sending. */
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13 to 19 digits")
    private String cardNumber;

    @NotBlank(message = "Cardholder name is required")
    @Size(max = 100, message = "Cardholder name cannot exceed 100 characters")
    private String cardHolderName;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;

    /** Four-digit year, e.g. 2027. */
    @NotNull(message = "Expiry year is required")
    @Min(value = 2000, message = "Enter a four-digit expiry year")
    @Max(value = 2100, message = "Enter a four-digit expiry year")
    private Integer expiryYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
    private String cvv;

    /**
     * User opted to save the card for next time. Honoured only when a real
     * gateway returns a reusable token - the simulated gateway has nothing
     * safe to store, so this is currently recorded intent, not a stored card.
     */
    private Boolean saveCard = false;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }
    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public Boolean getSaveCard() { return saveCard; }
    public void setSaveCard(Boolean saveCard) { this.saveCard = saveCard; }

    /**
     * Never let card data leak into logs or stack traces via toString().
     */
    @Override
    public String toString() {
        return "CardPaymentRequest{bookingId=" + bookingId + ", paymentMethod=" + paymentMethod + "}";
    }
}
