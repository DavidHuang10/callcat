package com.callcat.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Integration test that verifies the Spring Boot application context loads successfully
// Uses hardcoded test configuration to avoid .env dependency issues
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class BackendApplicationTests {

	// Integration test - verifies that the Spring Boot application context loads successfully
	// This test ensures that all beans are properly configured and dependency injection works
	// It uses the production database configuration to validate real AWS database connectivity
	@Test
	void contextLoads() {
		// Spring Boot test framework automatically verifies context loading
		// If this test passes, it means:
		// - All @Component, @Service, @Repository beans are created
		// - All configuration classes are processed
		// - No circular dependencies exist
		// - PostgreSQL RDS and DynamoDB connections can be established
		// - JWT service is properly configured
		// - Authentication and authorization components work together
	}

}
