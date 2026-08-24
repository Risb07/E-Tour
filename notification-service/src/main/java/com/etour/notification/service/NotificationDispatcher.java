package com.etour.notification.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.etour.notification.config.NotificationProperties;
import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationStatus;
import com.etour.notification.repository.NotificationRepository;
import com.etour.notification.transport.NotificationTransport;

/**
 * Drains the queue.
 *
 * <p>The retry policy is the whole point of this class, so it is worth stating
 * plainly: a message gets {@code maxAttempts} tries in total (default 2 - the
 * initial send and <b>one</b> retry). When the last one fails the row becomes
 * {@link NotificationStatus#FAILED} and this class never touches it again; only
 * an admin re-queueing it from the UI can give it more attempts.
 */
@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    /** MySQL's last_error column is 1000 chars; a stack-trace-laden message can exceed it. */
    private static final int MAX_ERROR_LENGTH = 1000;

    private final NotificationRepository notifications;
    private final NotificationTransport transport;
    private final NotificationProperties properties;

    public NotificationDispatcher(NotificationRepository notifications,
                                  NotificationTransport transport,
                                  NotificationProperties properties) {
        this.notifications = notifications;
        this.transport = transport;
        this.properties = properties;
    }

    /**
     * Claims a batch of due rows and attempts each one.
     *
     * <p>Runs in its own transaction: the {@code FOR UPDATE SKIP LOCKED} claim
     * only holds its locks for the life of that transaction, which is what stops
     * a second instance of this service picking up the same rows and sending
     * every message twice.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int dispatchDueBatch() {
        List<Notification> due = notifications.claimDueBatch(Instant.now(), properties.getBatchSize());
        if (due.isEmpty()) {
            return 0;
        }

        log.debug("Dispatching {} due notification(s)", due.size());
        for (Notification notification : due) {
            attemptDelivery(notification);
        }
        return due.size();
    }

    /** One delivery attempt, and the bookkeeping that follows it either way. */
    private void attemptDelivery(Notification notification) {
        notification.setAttempts(notification.getAttempts() + 1);
        notification.setTransport(transport.name());
        notification.setUpdatedAt(Instant.now());

        try {
            transport.send(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setNextAttemptAt(null);
            notification.setLastError(null);
            notifications.save(notification);

            log.info("Notification {} sent to {} on attempt {}/{}",
                    notification.getNotificationId(), notification.getRecipient(),
                    notification.getAttempts(), notification.getMaxAttempts());

        } catch (Exception ex) {
            recordFailure(notification, ex);
        }
    }

    private void recordFailure(Notification notification, Exception ex) {
        notification.setLastError(truncate(describe(ex)));

        if (notification.hasAttemptsLeft()) {
            // Attempts remain: stay PENDING and come back once, after the delay.
            notification.setStatus(NotificationStatus.PENDING);
            notification.setNextAttemptAt(
                    Instant.now().plus(properties.getRetryDelaySeconds(), ChronoUnit.SECONDS));

            log.warn("Notification {} failed on attempt {}/{} - retrying once at {}. Cause: {}",
                    notification.getNotificationId(), notification.getAttempts(),
                    notification.getMaxAttempts(), notification.getNextAttemptAt(),
                    notification.getLastError());
        } else {
            // Budget exhausted. Dead-letter it and stop - a human decides next.
            notification.setStatus(NotificationStatus.FAILED);
            notification.setNextAttemptAt(null);

            log.error("Notification {} to {} DEAD-LETTERED after {} attempt(s). Cause: {}",
                    notification.getNotificationId(), notification.getRecipient(),
                    notification.getAttempts(), notification.getLastError());
        }

        notifications.save(notification);
    }

    private static String describe(Exception ex) {
        // The cause is usually the informative part of a mail failure
        // ("Connection refused"); the wrapper class name rarely is.
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
        return root.getClass().getSimpleName() + ": " + message;
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_ERROR_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_ERROR_LENGTH - 3) + "...";
    }
}
