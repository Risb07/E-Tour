package com.etour.notification.transport;

import com.etour.notification.domain.Notification;

/**
 * How a rendered message actually leaves the building.
 *
 * <p>Implementations signal failure by throwing - the worker catches, records
 * the message on the row and decides retry vs dead-letter. They must never
 * swallow an error and return normally, because that would mark the message
 * SENT when it was not.
 */
public interface NotificationTransport {

    /** Short name recorded on the notification row, e.g. "smtp". */
    String name();

    /**
     * @throws Exception if delivery failed for any reason; the message becomes
     *                   the row's lastError.
     */
    void send(Notification notification) throws Exception;
}
