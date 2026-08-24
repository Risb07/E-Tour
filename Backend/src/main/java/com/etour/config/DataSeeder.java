package com.etour.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * See DemoDataSeederService for what actually gets seeded and why - the
 * work runs inside a real @Transactional service method (called through its
 * injected bean reference, not self-invocation) since it touches lazy JPA
 * collections that need an open Hibernate session.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDemoData(DemoDataSeederService seederService) {
        return args -> seederService.seedAll();
    }
}
