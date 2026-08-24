package com.etour.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.etour.notification.repository.NotificationTemplateRepository;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

    @Autowired private NotificationTemplateRepository templates;

    @Test
    @DisplayName("the context starts and the starter templates are seeded")
    void contextLoadsAndSeeds() {
        assertThat(templates.findByCode("BOOKING_CONFIRMED")).isPresent();
        assertThat(templates.findByCode("TEST_MESSAGE")).isPresent();
    }
}
