package com.etour.notification.domain;

/**
 * Lifecycle of a queued notification.
 *
 * <p>There is no "RETRYING" state on purpose: a row waiting for its retry is
 * still {@link #PENDING}, it just has a {@code nextAttemptAt} in the future.
 * Keeping the retry in the same state means the worker has exactly one query
 * to run and there is no way for a row to get stranded in a state nothing
 * polls.
 */
public enum NotificationStatus {

    /** Queued, or waiting for its one retry. The worker picks these up. */
    PENDING,

    /** Handed to the transport successfully. Terminal. */
    SENT,

    /**
     * Every allowed attempt failed - this is the dead letter. Terminal unless
     * an admin explicitly re-queues it from the UI.
     */
    FAILED
}
