package com.etour.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Everything tunable about delivery, bound from {@code notify.*}. */
@Component
@ConfigurationProperties(prefix = "notify")
public class NotificationProperties {

    /**
     * Total delivery attempts allowed per message, INCLUDING the first one.
     *
     * <p>Default 2 = the initial send plus exactly one retry, then the message
     * is dead-lettered for an admin to look at. This is a deliberate policy
     * choice rather than the usual exponential-backoff ladder: repeatedly
     * retrying a message whose recipient address is simply wrong just delays
     * the moment a human finds out, and duplicate delivery is worse than late
     * delivery for booking mail.
     */
    private int maxAttempts = 2;

    /** How long to wait before the single retry. */
    private long retryDelaySeconds = 30;

    /** How many due rows one worker pass claims. */
    private int batchSize = 25;

    /**
     * "smtp" or "simulated". Resolved automatically at startup when left blank:
     * smtp if mail credentials are configured, simulated otherwise - matching
     * how both eTour backends already degrade when mail is not set up.
     */
    private String transport = "";

    /** From address on outbound mail. */
    private String from = "noreply@etour.example.com";

    /**
     * Cap on the total attachment bytes in one message, enforced at enqueue.
     *
     * <p>10MB sits under the mediumblob column and under what mail servers
     * typically accept (Gmail rejects above ~25MB including base64 overhead).
     * Rejecting at enqueue matters: accepting an oversized message would queue
     * something that can only ever fail, burn both delivery attempts and land
     * in the dead-letter list for a reason the caller could have been told
     * synchronously.
     */
    private long maxAttachmentBytes = 10L * 1024 * 1024;

    private final Worker worker = new Worker();

    public static class Worker {
        /** Queue poll interval. Also referenced directly by the @Scheduled annotation. */
        private long pollIntervalMs = 5000;

        public long getPollIntervalMs() { return pollIntervalMs; }
        public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public long getMaxAttachmentBytes() { return maxAttachmentBytes; }
    public void setMaxAttachmentBytes(long maxAttachmentBytes) { this.maxAttachmentBytes = maxAttachmentBytes; }

    public Worker getWorker() { return worker; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public long getRetryDelaySeconds() { return retryDelaySeconds; }
    public void setRetryDelaySeconds(long retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
}
