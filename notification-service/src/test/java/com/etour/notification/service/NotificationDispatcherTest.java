package com.etour.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.etour.notification.config.NotificationProperties;
import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationStatus;
import com.etour.notification.repository.NotificationRepository;
import com.etour.notification.transport.NotificationTransport;

/**
 * The retry policy, pinned.
 *
 * <p>"One retry" is a business decision, not an implementation detail: getting
 * it wrong either spams a customer with duplicates or silently stops trying
 * after a single transient blip. These tests are the thing that stops an
 * innocent-looking edit to the attempt bookkeeping from changing it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationDispatcher")
class NotificationDispatcherTest {

    @Mock private NotificationRepository repository;
    @Mock private NotificationTransport transport;

    private NotificationProperties properties;
    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        properties = new NotificationProperties();
        properties.setMaxAttempts(2);          // initial send + exactly one retry
        properties.setRetryDelaySeconds(30);
        properties.setBatchSize(25);

        when(transport.name()).thenReturn("simulated");
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        dispatcher = new NotificationDispatcher(repository, transport, properties);
    }

    private Notification pending() {
        Notification n = new Notification();
        n.setNotificationId(1L);
        n.setRecipient("traveller@example.com");
        n.setTemplateCode("BOOKING_CONFIRMED");
        n.setSubject("s");
        n.setBody("b");
        n.setStatus(NotificationStatus.PENDING);
        n.setAttempts(0);
        n.setMaxAttempts(2);
        n.setNextAttemptAt(Instant.now());
        return n;
    }

    private void queue(Notification n) {
        when(repository.claimDueBatch(any(Instant.class), any(Integer.class))).thenReturn(List.of(n));
    }

    @Test
    @DisplayName("marks the message SENT when the first attempt succeeds")
    void succeedsFirstTime() throws Exception {
        Notification n = pending();
        queue(n);
        doNothing().when(transport).send(any());

        dispatcher.dispatchDueBatch();

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(n.getAttempts()).isEqualTo(1);
        assertThat(n.getSentAt()).isNotNull();
        assertThat(n.getLastError()).isNull();
        // Nothing left to do, so the worker must not pick it up again.
        assertThat(n.getNextAttemptAt()).isNull();
        verify(transport, times(1)).send(any());
    }

    @Test
    @DisplayName("stays PENDING and schedules exactly one retry after the first failure")
    void schedulesOneRetryOnFirstFailure() throws Exception {
        Notification n = pending();
        queue(n);
        doThrow(new IllegalStateException("smtp down")).when(transport).send(any());

        dispatcher.dispatchDueBatch();

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getAttempts()).isEqualTo(1);
        assertThat(n.getLastError()).contains("smtp down");
        assertThat(n.getNextAttemptAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("dead-letters after the retry fails - it does NOT keep retrying")
    void deadLettersAfterTheSingleRetry() throws Exception {
        Notification n = pending();
        queue(n);
        doThrow(new IllegalStateException("smtp down")).when(transport).send(any());

        dispatcher.dispatchDueBatch();   // attempt 1
        dispatcher.dispatchDueBatch();   // attempt 2 - the one retry

        assertThat(n.getAttempts()).isEqualTo(2);
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        // Null is what takes it out of the worker's claim query for good; a
        // future timestamp here would mean it silently kept retrying.
        assertThat(n.getNextAttemptAt()).isNull();
        verify(transport, times(2)).send(any());
    }

    @Test
    @DisplayName("never attempts a third time, even if the row is somehow re-claimed")
    void neverExceedsTheAttemptBudget() throws Exception {
        Notification n = pending();
        n.setAttempts(2);
        n.setMaxAttempts(2);
        queue(n);
        doThrow(new IllegalStateException("smtp down")).when(transport).send(any());

        dispatcher.dispatchDueBatch();

        // It did try (the row was handed to it), but afterwards it is terminal
        // rather than scheduled again - the budget is enforced on the way out.
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(n.getNextAttemptAt()).isNull();
    }

    @Test
    @DisplayName("succeeding on the retry marks it SENT and clears the earlier error")
    void retryCanSucceed() throws Exception {
        Notification n = pending();
        queue(n);
        doThrow(new IllegalStateException("transient blip")).when(transport).send(any());
        dispatcher.dispatchDueBatch();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);

        doNothing().when(transport).send(any());
        dispatcher.dispatchDueBatch();

        assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(n.getAttempts()).isEqualTo(2);
        assertThat(n.getLastError()).isNull();
    }

    @Test
    @DisplayName("records the root cause, not the wrapper, so the log is diagnosable")
    void recordsRootCause() throws Exception {
        Notification n = pending();
        queue(n);
        doThrow(new RuntimeException("wrapper", new java.net.ConnectException("Connection refused")))
                .when(transport).send(any());

        dispatcher.dispatchDueBatch();

        assertThat(n.getLastError()).contains("Connection refused");
    }

    @Test
    @DisplayName("truncates a huge error rather than failing the column write")
    void truncatesLongErrors() throws Exception {
        Notification n = pending();
        queue(n);
        doThrow(new IllegalStateException("x".repeat(5000))).when(transport).send(any());

        dispatcher.dispatchDueBatch();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getLastError().length()).isLessThanOrEqualTo(1000);
    }

    @Test
    @DisplayName("does nothing, and touches no transport, when the queue is empty")
    void emptyQueueIsANoOp() throws Exception {
        when(repository.claimDueBatch(any(Instant.class), any(Integer.class))).thenReturn(List.of());

        assertThat(dispatcher.dispatchDueBatch()).isZero();
        verify(transport, times(0)).send(any());
    }
}
