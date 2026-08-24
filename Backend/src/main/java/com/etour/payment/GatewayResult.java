package com.etour.payment;

import com.etour.enums.PaymentStatus;

/**
 * Outcome of a gateway charge attempt. Deliberately provider-agnostic: callers
 * branch on {@link PaymentStatus}, never on a provider-specific code.
 */
public class GatewayResult {

    private final PaymentStatus status;
    private final String transactionRef;
    private final String failureReason;

    private GatewayResult(PaymentStatus status, String transactionRef, String failureReason) {
        this.status = status;
        this.transactionRef = transactionRef;
        this.failureReason = failureReason;
    }

    public static GatewayResult success(String transactionRef) {
        return new GatewayResult(PaymentStatus.SUCCESS, transactionRef, null);
    }

    /** Provider accepted the request but settlement is not final yet (webhook to follow). */
    public static GatewayResult pending(String transactionRef) {
        return new GatewayResult(PaymentStatus.PENDING, transactionRef, null);
    }

    public static GatewayResult failed(String transactionRef, String reason) {
        return new GatewayResult(PaymentStatus.FAILED, transactionRef, reason);
    }

    public PaymentStatus getStatus() { return status; }
    public String getTransactionRef() { return transactionRef; }
    public String getFailureReason() { return failureReason; }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
}
