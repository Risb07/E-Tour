package com.etour.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationStatus;
import com.etour.notification.dto.NotificationDtos.SendRequest;
import com.etour.notification.repository.NotificationRepository;
import com.etour.notification.web.NotFoundException;

/** Enqueue, idempotency, template rendering and the manual re-queue path, on a real JPA stack. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Autowired private NotificationService service;
    @Autowired private NotificationRepository repository;

    private SendRequest request;

    @BeforeEach
    void setUp() {
        request = new SendRequest();
        request.setRecipient("traveller@example.com");
        request.setTemplate("BOOKING_CONFIRMED");
        request.setVariables(Map.of(
                "customerName", "Grace Hopper",
                "tourTitle", "The Rajasthan Edit",
                "bookingRef", "ETR-1001",
                "departureDate", "3 Oct 2026",
                "passengers", "2",
                "totalAmount", "Rs 77,800"));
    }

    @Test
    @DisplayName("renders the template at enqueue time, so a later template edit cannot rewrite history")
    void rendersOnEnqueue() {
        Notification queued = service.enqueue(request);

        assertThat(queued.getSubject()).isEqualTo("Your eTour booking ETR-1001 is confirmed");
        assertThat(queued.getBody())
                .contains("Grace Hopper")
                .contains("The Rajasthan Edit")
                .contains("Rs 77,800")
                // No placeholder may survive into something a customer reads.
                .doesNotContain("{{");
        assertThat(queued.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(queued.getAttempts()).isZero();
        assertThat(queued.getMaxAttempts()).isEqualTo(2);
    }

    @Test
    @DisplayName("a missing variable renders blank rather than leaving a raw placeholder")
    void missingVariableRendersBlank() {
        request.setVariables(Map.of("customerName", "Grace Hopper"));

        Notification queued = service.enqueue(request);

        assertThat(queued.getBody()).contains("Grace Hopper").doesNotContain("{{");
    }

    @Test
    @DisplayName("the same idempotency key queues exactly one message")
    void idempotencyKeyPreventsDoubleSend() {
        request.setIdempotencyKey("booking-1001-confirmed");

        Notification first = service.enqueue(request);
        Notification second = service.enqueue(request);

        assertThat(second.getNotificationId()).isEqualTo(first.getNotificationId());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("no idempotency key means the caller gets exactly what they asked for - two messages")
    void withoutKeyTwoRequestsQueueTwoMessages() {
        service.enqueue(request);
        service.enqueue(request);

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("an unknown template is a 404, not a blank email")
    void unknownTemplateIsRejected() {
        request.setTemplate("NOT_A_TEMPLATE");

        assertThatThrownBy(() -> service.enqueue(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Unknown template");
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("re-queueing a dead letter resets its attempt budget")
    void requeueResetsTheBudget() {
        Notification queued = service.enqueue(request);
        queued.setStatus(NotificationStatus.FAILED);
        queued.setAttempts(2);
        queued.setLastError("smtp down");
        repository.save(queued);

        Notification revived = service.requeue(queued.getNotificationId());

        assertThat(revived.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(revived.getAttempts()).isZero();
        assertThat(revived.getMaxAttempts()).isEqualTo(2);
        assertThat(revived.getLastError()).isNull();
        assertThat(revived.getNextAttemptAt()).isNotNull();
    }

    @Test
    @DisplayName("re-queueing something already sent is refused, so a customer cannot be mailed twice")
    void cannotRequeueASentMessage() {
        Notification queued = service.enqueue(request);
        queued.setStatus(NotificationStatus.SENT);
        repository.save(queued);

        assertThatThrownBy(() -> service.requeue(queued.getNotificationId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only a failed notification");
    }

    @Test
    @DisplayName("re-queueing a still-pending message is refused too")
    void cannotRequeueAPendingMessage() {
        Notification queued = service.enqueue(request);

        assertThatThrownBy(() -> service.requeue(queued.getNotificationId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
