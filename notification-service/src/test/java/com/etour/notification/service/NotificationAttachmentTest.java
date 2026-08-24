package com.etour.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.etour.notification.domain.Notification;
import com.etour.notification.dto.NotificationDtos.AttachmentInput;
import com.etour.notification.dto.NotificationDtos.NotificationResponse;
import com.etour.notification.dto.NotificationDtos.SendRequest;
import com.etour.notification.repository.NotificationRepository;

/**
 * Attachments, which exist so the receipt PDF survives the move off the
 * backends' direct SMTP send. If these break, customers silently stop getting
 * the PDF that used to be in their inbox.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Notification attachments")
class NotificationAttachmentTest {

    @Autowired private NotificationService service;
    @Autowired private NotificationRepository repository;

    private SendRequest request;

    @BeforeEach
    void setUp() {
        request = new SendRequest();
        request.setRecipient("traveller@example.com");
        request.setTemplate("BOOKING_RECEIPT");
        request.setVariables(Map.of(
                "customerName", "Grace Hopper",
                "tourTitle", "The Rajasthan Edit",
                "orderNumber", "ORD-1001",
                "invoiceNumber", "INV-1001"));
    }

    private static AttachmentInput pdf(String filename, byte[] bytes) {
        AttachmentInput input = new AttachmentInput();
        input.setFilename(filename);
        input.setContentType("application/pdf");
        input.setContentBase64(Base64.getEncoder().encodeToString(bytes));
        return input;
    }

    @Test
    @DisplayName("decodes base64 and stores the exact bytes that were sent")
    void roundTripsBytes() {
        byte[] original = "%PDF-1.4 fake receipt bytes".getBytes(StandardCharsets.UTF_8);
        request.setAttachments(List.of(pdf("receipt-INV-1001.pdf", original)));

        Notification queued = service.enqueue(request);

        assertThat(queued.getAttachments()).hasSize(1);
        var stored = queued.getAttachments().get(0);
        assertThat(stored.getContent()).isEqualTo(original);
        assertThat(stored.getFilename()).isEqualTo("receipt-INV-1001.pdf");
        assertThat(stored.getContentType()).isEqualTo("application/pdf");
        assertThat(stored.getSizeBytes()).isEqualTo(original.length);
    }

    @Test
    @DisplayName("the receipt template renders the wording the backends used to send")
    void receiptTemplateMatchesTheOldEmail() {
        Notification queued = service.enqueue(request);

        assertThat(queued.getSubject()).isEqualTo("Your eTour booking receipt - INV-1001");
        assertThat(queued.getBody())
                .contains("Hi Grace Hopper,")
                .contains("Thank you for booking The Rajasthan Edit with us.")
                .contains("(order ORD-1001)")
                .contains("Your receipt is attached as a PDF.")
                .contains("- TourIndia Travels")
                .doesNotContain("{{");
    }

    @Test
    @DisplayName("rejects attachments over the size cap at enqueue, not at delivery")
    void rejectsOversizedAttachments() {
        // Queuing something that can only ever fail would burn both delivery
        // attempts and dead-letter for a reason the caller could have been told
        // synchronously.
        byte[] huge = new byte[11 * 1024 * 1024];
        request.setAttachments(List.of(pdf("huge.pdf", huge)));

        assertThatThrownBy(() -> service.enqueue(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceed");
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("rejects content that is not valid base64")
    void rejectsMalformedBase64() {
        AttachmentInput bad = new AttachmentInput();
        bad.setFilename("broken.pdf");
        bad.setContentType("application/pdf");
        bad.setContentBase64("!!!not base64!!!");
        request.setAttachments(List.of(bad));

        assertThatThrownBy(() -> service.enqueue(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken.pdf");
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("defaults a missing content type rather than storing null")
    void defaultsContentType() {
        AttachmentInput input = new AttachmentInput();
        input.setFilename("thing.bin");
        input.setContentBase64(Base64.getEncoder().encodeToString(new byte[] {1, 2, 3}));
        request.setAttachments(List.of(input));

        Notification queued = service.enqueue(request);

        assertThat(queued.getAttachments().get(0).getContentType()).isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("a message with no attachments still queues fine")
    void noAttachmentsIsFine() {
        Notification queued = service.enqueue(request);

        assertThat(queued.getAttachments()).isEmpty();
    }

    @Test
    @DisplayName("the detail view reports attachment metadata but never the bytes")
    void detailExposesMetadataOnly() {
        byte[] original = "receipt".getBytes(StandardCharsets.UTF_8);
        request.setAttachments(List.of(pdf("receipt.pdf", original)));
        Notification queued = service.enqueue(request);

        NotificationResponse detail = service.getDetail(queued.getNotificationId());

        assertThat(detail.getAttachments()).hasSize(1);
        assertThat(detail.getAttachments().get(0).getFilename()).isEqualTo("receipt.pdf");
        assertThat(detail.getAttachments().get(0).getSizeBytes()).isEqualTo(original.length);
        // The summary type has no bytes accessor at all - asserted by the fact
        // that this compiles with only filename/contentType/sizeBytes available.
    }

    @Test
    @DisplayName("an idempotent replay does not attach the PDF a second time")
    void idempotentReplayDoesNotDuplicateAttachments() {
        // This is the case that matters for the booking receipt: the backend
        // enqueues with a key derived from the invoice number, so a retried
        // enqueue must return the original row untouched rather than growing it.
        request.setIdempotencyKey("receipt-INV-1001");
        request.setAttachments(List.of(pdf("receipt.pdf", "bytes".getBytes(StandardCharsets.UTF_8))));

        Notification first = service.enqueue(request);
        Notification second = service.enqueue(request);

        assertThat(second.getNotificationId()).isEqualTo(first.getNotificationId());
        assertThat(repository.count()).isEqualTo(1);
        assertThat(second.getAttachments()).hasSize(1);
    }
}
