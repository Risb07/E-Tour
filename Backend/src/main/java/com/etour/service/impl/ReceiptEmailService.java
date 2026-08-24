package com.etour.service.impl;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.etour.entity.Booking;
import com.etour.entity.Invoice;
import com.etour.entity.Passenger;
import com.etour.integration.NotificationClient;
import com.etour.repository.PassengerRepository;
import com.etour.util.ReceiptPdfGenerator;

/**
 * BRD 3.7 - "receipt will be displayed and e-mailed to user".
 *
 * <p>This no longer sends mail. It renders the receipt PDF and hands the whole
 * message to the notification microservice, which owns the queue, the retry and
 * the delivery log. The customer-visible result is unchanged: same subject, same
 * wording, same attached PDF - the template on the service side is a verbatim
 * copy of the text this class used to build.
 *
 * <h2>Why exactly one e-mail per booking</h2>
 * Three things have to hold, and each is handled in a different place:
 * <ol>
 *   <li><b>One payment per booking.</b> {@code PaymentServiceImpl} locks the
 *       booking row and refuses a second payment once it is CONFIRMED, so this
 *       method is reached once per booking to begin with.</li>
 *   <li><b>Never before the payment commits.</b> The enqueue is deferred to
 *       {@code afterCommit}. It used to send inline, which meant a rollback
 *       after the send left a customer holding a receipt for a payment that
 *       never happened.</li>
 *   <li><b>Never twice if step 2 runs twice.</b> The idempotency key is derived
 *       from the invoice number, so the notification service collapses a repeat
 *       enqueue onto the original message instead of queueing another.</li>
 * </ol>
 *
 * <p>Failures are logged, never thrown: a queue being unreachable is not a
 * reason to undo a payment that already succeeded, and the receipt stays
 * available from {@code GET /api/invoices/booking/{id}/receipt}.
 */
@Service
public class ReceiptEmailService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptEmailService.class);

    /** Seeded by the notification service; wording matches the old inline e-mail. */
    private static final String TEMPLATE = "BOOKING_RECEIPT";

    private final NotificationClient notificationClient;
    private final ReceiptPdfGenerator receiptPdfGenerator;
    private final PassengerRepository passengerRepository;

    public ReceiptEmailService(NotificationClient notificationClient,
            ReceiptPdfGenerator receiptPdfGenerator, PassengerRepository passengerRepository) {
        this.notificationClient = notificationClient;
        this.receiptPdfGenerator = receiptPdfGenerator;
        this.passengerRepository = passengerRepository;
    }

    public void sendReceiptEmail(Booking booking, Invoice invoice) {
        String toAddress = booking.getCustomer().getEmail();
        if (toAddress == null || toAddress.isBlank()) {
            log.warn("Skipping receipt e-mail for booking {} - customer has no e-mail on file",
                    booking.getBookingId());
            return;
        }

        // Built here, inside the transaction, while the entities are still
        // attached and their lazy associations loadable. Only the enqueue itself
        // is deferred.
        final String recipient = toAddress;
        final String idempotencyKey = "receipt-" + invoice.getInvoiceNumber();
        final Map<String, String> variables = Map.of(
                "customerName", nullToEmpty(booking.getCustomer().getFullName()),
                "tourTitle", nullToEmpty(booking.getSchedule().getTour().getTitle()),
                "orderNumber", nullToEmpty(booking.getOrderNumber()),
                "invoiceNumber", nullToEmpty(invoice.getInvoiceNumber()));

        final List<NotificationClient.Attachment> attachments = renderReceipt(booking, invoice);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // No transaction in progress (a direct call, or a test). Nothing to
            // wait for, so send now rather than silently dropping the message.
            enqueueQuietly(recipient, variables, idempotencyKey, attachments);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                enqueueQuietly(recipient, variables, idempotencyKey, attachments);
            }
        });
    }

    /**
     * The enqueue, with a belt-and-braces catch.
     *
     * <p>{@link NotificationClient} already swallows its own failures, so this
     * looks redundant - it is not. Both of this method's callers sit somewhere an
     * exception must never escape: one runs inside the payment transaction, and
     * the other inside {@code afterCommit}, where a throw is raised to whoever
     * committed. In both cases an unchecked exception from an unanticipated
     * source (a serialization problem, a misconfigured URL) would surface as a
     * failed payment for a customer whose money has already moved. The e-mail is
     * never worth that.
     */
    private void enqueueQuietly(String recipient, Map<String, String> variables,
            String idempotencyKey, List<NotificationClient.Attachment> attachments) {
        try {
            notificationClient.enqueue(recipient, TEMPLATE, variables, idempotencyKey, attachments);
        } catch (Exception e) {
            log.warn("Could not queue the receipt e-mail for {} (key {}): {}",
                    recipient, idempotencyKey, e.getMessage());
        }
    }

    /**
     * @return the receipt as a single-element attachment list, or an empty list
     *         if it could not be produced - a missing PDF must not stop the
     *         confirmation e-mail, since the customer can still download it.
     */
    private List<NotificationClient.Attachment> renderReceipt(Booking booking, Invoice invoice) {
        try {
            List<Passenger> passengers = passengerRepository.findByBooking_BookingId(booking.getBookingId());
            byte[] pdf = receiptPdfGenerator.generate(booking, invoice, passengers);

            return List.of(new NotificationClient.Attachment(
                    "receipt-" + invoice.getInvoiceNumber() + ".pdf",
                    "application/pdf",
                    Base64.getEncoder().encodeToString(pdf)));

        } catch (Exception e) {
            log.warn("Could not render the receipt PDF for booking {} - queueing the e-mail without it: {}",
                    booking.getBookingId(), e.getMessage());
            return List.of();
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
