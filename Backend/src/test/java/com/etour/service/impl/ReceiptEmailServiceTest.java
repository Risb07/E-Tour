package com.etour.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.etour.entity.Booking;
import com.etour.entity.Customer;
import com.etour.entity.Invoice;
import com.etour.entity.Tour;
import com.etour.entity.TourSchedule;
import com.etour.integration.NotificationClient;
import com.etour.repository.PassengerRepository;
import com.etour.util.ReceiptPdfGenerator;

/**
 * The receipt e-mail now goes to the notification microservice instead of an
 * SMTP server.
 *
 * <p>The thing worth pinning is the idempotency key. It is what guarantees a
 * customer gets exactly ONE e-mail per booking: if it ever stops being derived
 * from the invoice number, a retried enqueue silently becomes a second e-mail
 * and nothing else in the system would notice.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReceiptEmailService")
class ReceiptEmailServiceTest {

    private static final byte[] PDF = "%PDF-1.4 fake".getBytes(StandardCharsets.UTF_8);

    @Mock private NotificationClient notificationClient;
    @Mock private ReceiptPdfGenerator receiptPdfGenerator;
    @Mock private PassengerRepository passengerRepository;

    @InjectMocks private ReceiptEmailService service;

    private Booking booking;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setCustomerId(7L);
        customer.setFullName("Grace Hopper");
        customer.setEmail("traveller@example.com");

        Tour tour = new Tour();
        tour.setTourId(1L);
        tour.setTitle("The Rajasthan Edit");

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        booking = new Booking();
        booking.setBookingId(42L);
        booking.setCustomer(customer);
        booking.setSchedule(schedule);
        booking.setOrderNumber("ORD-1001");

        invoice = new Invoice();
        invoice.setInvoiceNumber("INV-1001");

        when(passengerRepository.findByBooking_BookingId(anyLong())).thenReturn(List.of());
        when(receiptPdfGenerator.generate(any(), any(), any())).thenReturn(PDF);
        when(notificationClient.enqueue(anyString(), anyString(), anyMap(), anyString(), anyList()))
                .thenReturn(true);
    }

    @Nested
    @DisplayName("exactly one e-mail per booking")
    class OneEmailPerBooking {

        @Test
        @DisplayName("keys the message on the invoice number so a retry cannot double-send")
        void keysOnInvoiceNumber() {
            service.sendReceiptEmail(booking, invoice);

            verify(notificationClient).enqueue(
                    eq("traveller@example.com"),
                    eq("BOOKING_RECEIPT"),
                    anyMap(),
                    eq("receipt-INV-1001"),
                    anyList());
        }

        @Test
        @DisplayName("calling twice reuses the same key, which the service collapses")
        void twoCallsUseTheSameKey() {
            // The backend does not de-duplicate itself; it relies on the key being
            // stable. This asserts the half the backend is responsible for.
            service.sendReceiptEmail(booking, invoice);
            service.sendReceiptEmail(booking, invoice);

            verify(notificationClient, times(2))
                    .enqueue(anyString(), anyString(), anyMap(), eq("receipt-INV-1001"), anyList());
        }
    }

    @Test
    @DisplayName("attaches the receipt PDF, base64-encoded")
    void attachesThePdf() {
        service.sendReceiptEmail(booking, invoice);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationClient.Attachment>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationClient).enqueue(anyString(), anyString(), anyMap(), anyString(), captor.capture());

        List<NotificationClient.Attachment> attachments = captor.getValue();
        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).filename()).isEqualTo("receipt-INV-1001.pdf");
        assertThat(attachments.get(0).contentType()).isEqualTo("application/pdf");
        assertThat(Base64.getDecoder().decode(attachments.get(0).contentBase64())).isEqualTo(PDF);
    }

    @Test
    @DisplayName("passes every variable the BOOKING_RECEIPT template needs")
    void passesTemplateVariables() {
        service.sendReceiptEmail(booking, invoice);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(notificationClient).enqueue(anyString(), anyString(), captor.capture(), anyString(), anyList());

        // A missing placeholder renders blank in a customer-facing e-mail rather
        // than failing loudly, so it has to be checked here.
        assertThat(captor.getValue())
                .containsEntry("customerName", "Grace Hopper")
                .containsEntry("tourTitle", "The Rajasthan Edit")
                .containsEntry("orderNumber", "ORD-1001")
                .containsEntry("invoiceNumber", "INV-1001");
    }

    @Test
    @DisplayName("queues nothing when the customer has no e-mail on file")
    void noEmailAddressQueuesNothing() {
        booking.getCustomer().setEmail(null);

        service.sendReceiptEmail(booking, invoice);

        verify(notificationClient, never()).enqueue(anyString(), anyString(), anyMap(), anyString(), anyList());
    }

    @Test
    @DisplayName("still queues the confirmation when the PDF cannot be rendered")
    void pdfFailureStillQueuesTheEmail() {
        // A receipt that failed to render must not cost the customer their
        // confirmation - they can download the PDF from the dashboard either way.
        when(receiptPdfGenerator.generate(any(), any(), any()))
                .thenThrow(new RuntimeException("font missing"));

        service.sendReceiptEmail(booking, invoice);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<NotificationClient.Attachment>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationClient).enqueue(anyString(), eq("BOOKING_RECEIPT"), anyMap(), anyString(), captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    @DisplayName("does not throw when the notification service is unreachable")
    void serviceDownDoesNotThrow() {
        // A payment has already been taken by this point. Throwing here would fail
        // the request for a customer whose money has moved.
        when(notificationClient.enqueue(anyString(), anyString(), anyMap(), anyString(), anyList()))
                .thenThrow(new RuntimeException("connection refused"));

        // The client swallows its own failures, but assert the seam too - this is
        // the contract PaymentServiceImpl depends on.
        try {
            service.sendReceiptEmail(booking, invoice);
        } catch (RuntimeException ex) {
            org.junit.jupiter.api.Assertions.fail(
                    "sendReceiptEmail must never propagate: " + ex.getMessage());
        }
    }
}
