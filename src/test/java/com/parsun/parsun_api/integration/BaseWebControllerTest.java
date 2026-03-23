package com.parsun.parsun_api.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@ActiveProfiles("web-test")
public abstract class BaseWebControllerTest {

    private static final Path TEMP_STORAGE_DIR;

    static {
        try {
            TEMP_STORAGE_DIR = Files.createTempDirectory("parsun-web-test-uploads");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp storage directory", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.type", () -> "local");
        registry.add("storage.location", () -> TEMP_STORAGE_DIR.toString());
    }
}
