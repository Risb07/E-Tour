package com.etour.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * eTour notification service.
 *
 * <p>A standalone microservice that owns everything about outbound
 * notifications: the templates, the queue, the delivery attempts and the
 * dead-letter list. It has its <b>own database</b> ({@code etour_notify}, with
 * its own MySQL user) and shares no tables with the eTour backends - although
 * both databases live on one MySQL server. The only things it borrows from
 * them are the JWT signing secret (so a token minted by either backend
 * verifies here) and, for admin authorisation only, one HTTP call to whichever
 * backend is currently live.
 *
 * <p>Deliberately additive: neither the Java nor the .NET backend was changed
 * to add this service, and both keep sending their own receipt mail exactly as
 * before. Nothing here is on their critical path.
 */
@SpringBootApplication
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
