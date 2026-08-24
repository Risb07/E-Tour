package com.etour.payment;

import java.time.YearMonth;

/**
 * Card data passed to a gateway for a single charge. Deliberately NOT an
 * entity and never persisted.
 *
 * {@link #toString()} is overridden so a stray log statement or an exception
 * stack trace can never print a PAN or CVV.
 */
public class CardDetails {

    private final String number;
    private final String holderName;
    private final int expiryMonth;
    private final int expiryYear;
    private final String cvv;

    public CardDetails(String number, String holderName, int expiryMonth, int expiryYear, String cvv) {
        this.number = number == null ? null : number.replaceAll("\\s|-", "");
        this.holderName = holderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
    }

    public String getNumber() { return number; }
    public String getHolderName() { return holderName; }
    public int getExpiryMonth() { return expiryMonth; }
    public int getExpiryYear() { return expiryYear; }
    public String getCvv() { return cvv; }

    /** Last four digits - the only part of the number safe to keep. */
    public String getLast4() {
        if (number == null || number.length() < 4) return "____";
        return number.substring(number.length() - 4);
    }

    /**
     * Brand inferred from the IIN/BIN prefix. Presentational only - the
     * gateway is the authority on whether a card is actually usable.
     */
    public String getBrand() {
        if (number == null || number.isEmpty()) return "CARD";
        if (number.startsWith("4")) return "VISA";
        if (number.matches("^5[1-5].*") || number.matches("^2[2-7].*")) return "MASTERCARD";
        if (number.matches("^3[47].*")) return "AMEX";
        if (number.startsWith("6")) return "DISCOVER";
        if (number.startsWith("35")) return "JCB";
        if (number.matches("^(60|65|81|82|508).*")) return "RUPAY";
        return "CARD";
    }

    /** e.g. "VISA ****4242" - safe to store and display. */
    public String getMaskedSummary() {
        return getBrand() + " ****" + getLast4();
    }

    /** Luhn check digit validation. */
    public boolean passesLuhn() {
        if (number == null || !number.matches("\\d{12,19}")) return false;
        int sum = 0;
        boolean doubleIt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    /** A card is valid through the LAST day of its expiry month. */
    public boolean isExpired(YearMonth now) {
        if (expiryMonth < 1 || expiryMonth > 12) return true;
        return YearMonth.of(expiryYear, expiryMonth).isBefore(now);
    }

    /** AMEX uses a 4-digit CID; everything else uses 3. */
    public boolean hasValidCvv() {
        if (cvv == null) return false;
        int expected = "AMEX".equals(getBrand()) ? 4 : 3;
        return cvv.matches("\\d{" + expected + "}");
    }

    @Override
    public String toString() {
        // Never expose the PAN or CVV.
        return "CardDetails{" + getMaskedSummary() + "}";
    }
}
