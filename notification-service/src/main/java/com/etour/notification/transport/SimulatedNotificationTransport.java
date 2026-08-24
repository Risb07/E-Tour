package com.etour.notification.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etour.notification.domain.Notification;

/**
 * Logs instead of sending, for deployments with no mail credentials - the same
 * way both eTour backends already degrade when {@code MAIL_USERNAME} is blank.
 *
 * <p>One deliberate exception: a recipient whose local part starts with
 * {@code fail} always throws. Without it there is no way to exercise the retry
 * and dead-letter paths on a machine with no SMTP server, which are exactly the
 * paths worth demonstrating. It is scoped to this transport, so it can never
 * affect real delivery.
 */
public class SimulatedNotificationTransport implements NotificationTransport {

    private static final Logger log = LoggerFactory.getLogger(SimulatedNotificationTransport.class);

    /** Recipients starting with this local part are treated as permanently undeliverable. */
    public static final String FAILURE_TRIGGER_PREFIX = "fail";

    @Override
    public String name() {
        return "simulated";
    }

    @Override
    public void send(Notification notification) {
        String recipient = notification.getRecipient();
        if (recipient != null && recipient.toLowerCase().startsWith(FAILURE_TRIGGER_PREFIX)) {
            throw new IllegalStateException(
                    "Simulated permanent delivery failure for " + recipient
                            + " (recipients starting with \"" + FAILURE_TRIGGER_PREFIX
                            + "\" always fail in simulated transport)");
        }

        var attachments = notification.getAttachments();
        // Touching the lazy collection here is deliberate as well as informative:
        // it means the simulated path exercises the same loading the SMTP
        // transport depends on, so a mapping mistake shows up in dev too.
        int attachmentCount = attachments == null ? 0 : attachments.size();

        log.info("[simulated] would send to={} subject=\"{}\" attachments={}",
                recipient, notification.getSubject(), attachmentCount);

        if (attachmentCount > 0) {
            attachments.forEach(a ->
                    log.info("[simulated]   attachment: {} ({}, {} bytes)",
                            a.getFilename(), a.getContentType(), a.getSizeBytes()));
        }
    }
}
