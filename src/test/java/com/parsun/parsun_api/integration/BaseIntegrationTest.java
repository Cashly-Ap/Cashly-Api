package com.parsun.parsun_api.integration;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    private static final Path TEMP_STORAGE_DIR;

    static {
        try {
            TEMP_STORAGE_DIR = Files.createTempDirectory("parsun-test-uploads");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp storage directory", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Use pre-provisioned mysql-test container from docker-compose
        // instead of Testcontainers (incompatible with Docker Desktop socket)
        registry.add("spring.datasource.url", () -> "jdbc:mysql://mysql-test:3306/parsun_test?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_0900_ai_ci");
        registry.add("spring.datasource.username", () -> "testuser");
        registry.add("spring.datasource.password", () -> "testpass");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("storage.type", () -> "local");
        registry.add("storage.location", () -> TEMP_STORAGE_DIR.toString());
    }
}
