package com.etour.integration;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.etour.security.JwtService;

/**
 * Talks to the eTour notification microservice.
 *
 * <p>This backend no longer sends mail itself - it hands the message to the
 * notification service, which owns the queue, the templates and the single
 * retry. See {@code ../../notification-service/README.md}.
 *
 * <h2>How it authenticates</h2>
 * The notification service accepts any token signed with the shared
 * {@code JWT_SECRET}, so this mints one for itself through the same
 * {@link JwtService} that issues customer tokens. No new credential to
 * configure, and no shared API key to leak.
 *
 * <p>The subject is a reserved internal address that can never match a real
 * account, which gives a useful least-privilege result: the service's admin
 * endpoints re-check the ADMIN role against this backend's own database, and a
 * subject with no user row fails that check. So this token can <em>enqueue</em>
 * and nothing else - it cannot read the delivery log.
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    /** Not a real, or registrable, mailbox - see the class comment. */
    private static final String SERVICE_SUBJECT = "system@etour.internal";

    private final RestClient restClient;
    private final JwtService jwtService;
    private final boolean enabled;

    public NotificationClient(JwtService jwtService,
                              @Value("${app.notification.base-url:http://notification-service:8085}") String baseUrl,
                              @Value("${app.notification.enabled:true}") boolean enabled) {
        // RestClient.builder() rather than an injected RestClient.Builder: that
        // bean comes from an auto-configuration this application does not pull
        // in, and depending on it fails the whole context at startup rather than
        // just this one integration.
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.jwtService = jwtService;
        this.enabled = enabled;

        if (!enabled) {
            log.warn("Notification service integration is DISABLED - no e-mail will be queued.");
        }
    }

    /**
     * Queues a message. Best-effort by contract: this is called after a payment
     * has already committed, so a failure here is logged and swallowed rather
     * than thrown.
     *
     * <p>There is deliberately <b>no fallback to direct SMTP</b>. If the enqueue
     * request fails after the service actually accepted it - a lost response,
     * a timeout on a request that landed - a fallback send would put a second
     * copy of the same e-mail in the customer's inbox. The receipt stays
     * available from {@code GET /api/invoices/booking/{id}/receipt}, and the
     * admin Notifications screen shows anything that did not go out.
     *
     * @param idempotencyKey stable per logical message, so a retry can never
     *                       produce a duplicate e-mail
     * @return true if the service accepted the message
     */
    public boolean enqueue(String recipient, String template, Map<String, String> variables,
                           String idempotencyKey, List<Attachment> attachments) {
        if (!enabled) {
            return false;
        }

        try {
            Map<String, Object> body = Map.of(
                    "recipient", recipient,
                    "template", template,
                    "variables", variables == null ? Map.of() : variables,
                    "idempotencyKey", idempotencyKey,
                    "attachments", attachments == null ? List.of() : attachments);

            restClient.post()
                    .uri("/svc/notifications")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(SERVICE_SUBJECT))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Queued '{}' notification to {} (idempotency key {})", template, recipient, idempotencyKey);
            return true;

        } catch (Exception ex) {
            log.warn("Could not queue '{}' notification to {} (key {}): {}",
                    template, recipient, idempotencyKey, ex.getMessage());
            return false;
        }
    }

    /**
     * An attachment on the wire. Base64 because the notification API is JSON.
     *
     * <p>A record, so Jackson serializes the component names directly and they
     * match the service's {@code AttachmentInput} field-for-field.
     */
    public record Attachment(String filename, String contentType, String contentBase64) {
    }
}
