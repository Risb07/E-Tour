package com.etour.notification.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.etour.notification.domain.NotificationTemplate;
import com.etour.notification.repository.NotificationTemplateRepository;

/**
 * Seeds the starter templates.
 *
 * <p>Idempotent by template code: an existing template is left exactly as it is,
 * so an admin's edits survive every restart. Only genuinely missing codes are
 * inserted.
 */
@Configuration
public class TemplateSeeder {

    private static final Logger log = LoggerFactory.getLogger(TemplateSeeder.class);

    @Bean
    public ApplicationRunner seedNotificationTemplates(NotificationTemplateRepository repository) {
        return args -> {
            List<NotificationTemplate> defaults = List.of(
                    // Wording copied VERBATIM from the backends' old
                    // ReceiptEmailService. Both now enqueue through here instead
                    // of sending directly, and a customer should not be able to
                    // tell that anything changed.
                    new NotificationTemplate(
                            "BOOKING_RECEIPT",
                            "Payment receipt, sent with the invoice PDF attached. Enqueued by both backends after a successful payment.",
                            "Your eTour booking receipt - {{invoiceNumber}}",
                            """
                            Hi {{customerName}},

                            Thank you for booking {{tourTitle}} with us. Your payment was successful and your booking is confirmed (order {{orderNumber}}).

                            Your receipt is attached as a PDF.

                            - TourIndia Travels
                            """),

                    new NotificationTemplate(
                            "BOOKING_CONFIRMED",
                            "Sent when a booking is confirmed and paid",
                            "Your eTour booking {{bookingRef}} is confirmed",
                            """
                            Hi {{customerName}},

                            Your booking for {{tourTitle}} is confirmed.

                            Booking reference: {{bookingRef}}
                            Departure: {{departureDate}}
                            Passengers: {{passengers}}
                            Total paid: {{totalAmount}}

                            You can download your receipt from your eTour dashboard.

                            Safe travels,
                            The eTour team
                            """),

                    new NotificationTemplate(
                            "BOOKING_CANCELLED",
                            "Sent when a booking is cancelled",
                            "Your eTour booking {{bookingRef}} has been cancelled",
                            """
                            Hi {{customerName}},

                            Your booking {{bookingRef}} for {{tourTitle}} has been cancelled.

                            Any refund due will be processed to your original payment method.

                            The eTour team
                            """),

                    new NotificationTemplate(
                            "DEPARTURE_REMINDER",
                            "Reminder ahead of the departure date",
                            "{{tourTitle}} departs on {{departureDate}}",
                            """
                            Hi {{customerName}},

                            A quick reminder that {{tourTitle}} departs on {{departureDate}}.

                            Booking reference: {{bookingRef}}

                            Please re-check your documents and arrive at the meeting point on time.

                            The eTour team
                            """),

                    new NotificationTemplate(
                            "PAYMENT_FAILED",
                            "Sent when a card payment is declined",
                            "Payment for booking {{bookingRef}} was declined",
                            """
                            Hi {{customerName}},

                            The payment for booking {{bookingRef}} was declined and the booking
                            is not yet confirmed.

                            You can retry the payment from your eTour dashboard.

                            The eTour team
                            """),

                    new NotificationTemplate(
                            "TEST_MESSAGE",
                            "Free-text message, used to try the queue from the admin screen",
                            "{{subject}}",
                            """
                            {{message}}

                            - sent by the eTour notification service
                            """));

            int inserted = 0;
            for (NotificationTemplate template : defaults) {
                if (!repository.existsByCode(template.getCode())) {
                    repository.save(template);
                    inserted++;
                }
            }

            if (inserted > 0) {
                log.info("Seeded {} notification template(s)", inserted);
            }
        };
    }
}
