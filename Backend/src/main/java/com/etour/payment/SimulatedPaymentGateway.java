package com.etour.payment;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default gateway: always succeeds, no money moves. This is what the project
 * has always done - it's now behind the {@link PaymentGateway} interface so a
 * real provider can replace it without touching PaymentServiceImpl.
 *
 * Active unless {@code app.payment.provider} is set to something else, so
 * existing behaviour is unchanged out of the box.
 */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    @Override
    public String getProviderName() {
        return "SIMULATED";
    }

    /**
     * Test cards, so the failure paths can actually be exercised without a
     * real provider. A real gateway decides this server-side; these prefixes
     * only exist in the simulator.
     */
    private static final String DECLINE_CARD_PREFIX = "4000000000000002";
    private static final String INSUFFICIENT_FUNDS_PREFIX = "4000000000009995";

    @Override
    public GatewayResult charge(GatewayChargeRequest request) {
        // Guard against a zero/negative charge reaching a real provider later -
        // catching it here means the same rule applies to every gateway.
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return GatewayResult.failed(null, "Amount must be greater than zero");
        }

        CardDetails card = request.getCard();
        if (card != null) {
            // Re-check server-side. The browser already validated, but a client
            // can be bypassed entirely - never trust it as the only gate.
            if (!card.passesLuhn()) {
                return GatewayResult.failed(null, "The card number is not valid");
            }
            if (card.isExpired(YearMonth.now())) {
                return GatewayResult.failed(null, "The card has expired");
            }
            if (!card.hasValidCvv()) {
                return GatewayResult.failed(null, "The security code is not valid");
            }
            if (card.getNumber().startsWith(DECLINE_CARD_PREFIX)) {
                return GatewayResult.failed("TXN-" + UUID.randomUUID(), "The card was declined by the issuer");
            }
            if (card.getNumber().startsWith(INSUFFICIENT_FUNDS_PREFIX)) {
                return GatewayResult.failed("TXN-" + UUID.randomUUID(), "Insufficient funds");
            }
        }

        // Logs the masked summary only - never the PAN.
        log.info("[SIMULATED] Charging {} for booking {} via {} {}",
                request.getAmount(), request.getBookingId(), request.getPaymentMethod(),
                card == null ? "" : card.getMaskedSummary());

        return GatewayResult.success("TXN-" + UUID.randomUUID());
    }
}
