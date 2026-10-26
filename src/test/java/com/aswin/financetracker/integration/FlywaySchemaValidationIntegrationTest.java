package com.aswin.financetracker.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.datasource.url=jdbc:h2:mem:flywaytest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class FlywaySchemaValidationIntegrationTest {

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    @DisplayName("Flyway migrations execute cleanly and Hibernate validates schema with ddl-auto=validate")
    void flywayMigrationsAndHibernateValidateSuccess() {
        assertNotNull(flyway, "Flyway bean must be present and active");
        assertTrue(flyway.info().applied().length >= 2, "V1 and V2 migrations must be applied successfully");
    }
}
