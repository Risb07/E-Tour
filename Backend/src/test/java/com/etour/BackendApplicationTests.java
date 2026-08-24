package com.etour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: the whole Spring context wires up.
 *
 * Runs against the in-memory H2 database configured in
 * src/test/resources/application-test.properties. Before that profile existed
 * this test booted against the real MySQL URL from application.properties and
 * failed with "Access denied for user 'root'" on any machine that had not
 * exported DB_PASSWORD - which is why the build had to be run with
 * -DskipTests.
 *
 * It is worth more than it looks: a missing bean, a bad @Value, a broken
 * component scan or an entity that does not map will all fail here.
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@Test
	@DisplayName("Application context loads with all beans wired")
	void contextLoads() {
	}

}
