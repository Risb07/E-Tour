package com.etour.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The timer that drives {@link NotificationDispatcher}.
 *
 * <p>Split from the dispatcher so the delivery logic can be unit-tested by
 * calling it directly, with no scheduler involved and no waiting.
 */
@Component
public class NotificationWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorker.class);

    private final NotificationDispatcher dispatcher;

    public NotificationWorker(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * fixedDelay, not fixedRate: the next poll is measured from the end of the
     * previous one, so a slow SMTP server cannot cause passes to pile up on top
     * of each other.
     */
    @Scheduled(fixedDelayString = "${notify.worker.poll-interval-ms:5000}")
    public void pollQueue() {
        try {
            dispatcher.dispatchDueBatch();
        } catch (Exception ex) {
            // Never let an exception escape a @Scheduled method: Spring's
            // scheduler cancels the task permanently if one does, and the queue
            // would silently stop draining for the life of the process.
            log.error("Notification queue poll failed; will retry on the next tick", ex);
        }
    }
}
