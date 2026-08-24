package com.etour.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import com.etour.notification.transport.NotificationTransport;
import com.etour.notification.transport.SimulatedNotificationTransport;
import com.etour.notification.transport.SmtpNotificationTransport;

/**
 * Picks the transport once, at startup.
 *
 * <p>Auto-detection (the default) mirrors what both eTour backends already do:
 * with no mail username configured they skip sending and log instead, rather
 * than failing. Doing the same here means the service is useful out of the box
 * on a laptop with no SMTP server, and switches to real delivery the moment
 * credentials appear in {@code .env} - with no code or config change.
 */
@Configuration
public class TransportConfig {

    private static final Logger log = LoggerFactory.getLogger(TransportConfig.class);

    @Bean
    public NotificationTransport notificationTransport(NotificationProperties properties,
                                                       JavaMailSender mailSender,
                                                       @Value("${spring.mail.username:}") String mailUsername) {

        String configured = properties.getTransport() == null ? "" : properties.getTransport().trim();

        boolean useSmtp = configured.isEmpty()
                ? !mailUsername.isBlank()
                : configured.equalsIgnoreCase("smtp");

        if (useSmtp) {
            log.info("Notification transport: smtp (from={})", properties.getFrom());
            return new SmtpNotificationTransport(mailSender, properties.getFrom());
        }

        log.warn("Notification transport: simulated - messages are logged, not delivered. "
                + "Set MAIL_USERNAME/MAIL_PASSWORD to send for real.");
        return new SimulatedNotificationTransport();
    }
}
