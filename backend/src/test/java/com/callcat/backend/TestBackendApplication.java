package com.callcat.backend;

import com.callcat.backend.config.TestAwsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Test configuration that includes test-specific configurations.
 * This ensures all Spring Boot tests use mock AWS services instead of real ones.
 */
@TestConfiguration
@Import(TestAwsConfig.class)
public class TestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestBackendApplication.class, args);
    }
}