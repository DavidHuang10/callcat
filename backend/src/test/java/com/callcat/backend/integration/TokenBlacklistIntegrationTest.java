package com.callcat.backend.integration;

import com.callcat.backend.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for token blacklist functionality
// These tests run against the actual Spring Boot application context
// They test the complete integration between service, repository, and DynamoDB
// 
// To run these tests with real DynamoDB:
// 1. Set environment variable: DYNAMODB_INTEGRATION_TESTS=true
// 2. Ensure DynamoDB Local is running: docker run -p 8000:8000 amazon/dynamodb-local -jar DynamoDBLocal.jar -inMemory -sharedDb
// 3. Run tests with: mvn test -Dtest=TokenBlacklistIntegrationTest
@SpringBootTest(classes = com.callcat.backend.TestBackendApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "aws.region=us-east-1",
    "aws.accessKeyId=test",
    "aws.secretAccessKey=test",
    "aws.dynamodb.endpoint=http://localhost:8000"
})
@EnabledIfEnvironmentVariable(named = "DYNAMODB_INTEGRATION_TESTS", matches = "true",
    disabledReason = "DynamoDB integration tests require DynamoDB Local to be running. " +
                    "Set DYNAMODB_INTEGRATION_TESTS=true environment variable to enable.")
class TokenBlacklistIntegrationTest {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // Tests complete token blacklisting workflow
    // Verifies end-to-end functionality from service to DynamoDB storage
    @Test
    void tokenBlacklistWorkflow_ShouldWorkEndToEnd() {
        // Arrange
        String testToken = "integration.test.jwt.token.12345";

        // Act & Assert - Initially not blacklisted
        assertFalse(tokenBlacklistService.isTokenBlacklisted(testToken),
                "Token should not be blacklisted initially");

        // Act - Blacklist the token
        tokenBlacklistService.blacklistToken(testToken);

        // Assert - Now should be blacklisted
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken),
                "Token should be blacklisted after blacklisting");

        // Act - Blacklist again (test idempotency)
        tokenBlacklistService.blacklistToken(testToken);

        // Assert - Should still be blacklisted
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken),
                "Token should remain blacklisted after duplicate blacklisting");
    }

    // Tests blacklisting multiple different tokens
    // Verifies that the system can handle multiple tokens independently
    @Test
    void multipleTokenBlacklisting_ShouldWorkIndependently() {
        // Arrange
        String[] testTokens = {
            "integration.token.alpha.001",
            "integration.token.beta.002",
            "integration.token.gamma.003"
        };

        // Act - Blacklist all tokens
        for (String token : testTokens) {
            tokenBlacklistService.blacklistToken(token);
        }

        // Assert - All tokens should be blacklisted
        for (String token : testTokens) {
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token),
                    "Token " + token + " should be blacklisted");
        }

        // Assert - A different token should not be blacklisted
        String differentToken = "different.token.not.blacklisted";
        assertFalse(tokenBlacklistService.isTokenBlacklisted(differentToken),
                "Different token should not be blacklisted");
    }

    // Tests realistic JWT-like token blacklisting
    // Uses a token format similar to real JWT tokens
    @Test
    void realisticJwtTokenBlacklisting_ShouldWork() {
        // Arrange - Simulate a real JWT token structure
        String jwtLikeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                             "eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzM5Mjg5NjAwLCJleHAiOjE3MzkyOTMyMDB9." +
                             "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c_integration_test";

        // Act
        tokenBlacklistService.blacklistToken(jwtLikeToken);

        // Assert
        assertTrue(tokenBlacklistService.isTokenBlacklisted(jwtLikeToken),
                "JWT-like token should be successfully blacklisted");
    }

    // Tests performance with rapid operations
    // Verifies that the system can handle multiple quick operations
    @Test
    void rapidTokenOperations_ShouldPerformAdequately() {
        // Arrange
        String baseToken = "performance.test.token.";
        int numberOfOperations = 5;

        // Act & Assert
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfOperations; i++) {
            String token = baseToken + i;
            tokenBlacklistService.blacklistToken(token);
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Assert reasonable performance (less than 3 seconds for 5 operations)
        assertTrue(duration < 3000, 
                "Operations took too long: " + duration + "ms for " + numberOfOperations + " operations");
    }

    // Tests error resilience
    // Verifies that the system handles edge cases gracefully
    @Test
    void edgeCaseTokens_ShouldHandleGracefully() {
        // Arrange - Test various edge case tokens
        String[] edgeCaseTokens = {
            "token.with.many.dots.and.segments",
            "token-with-dashes-and-123-numbers",
            "token_with_underscores_456",
            "UPPERCASE.TOKEN.789",
            "mixed.Case.Token.ABC123def"
        };

        // Act & Assert
        for (String token : edgeCaseTokens) {
            assertDoesNotThrow(() -> {
                tokenBlacklistService.blacklistToken(token);
                assertTrue(tokenBlacklistService.isTokenBlacklisted(token),
                        "Edge case token should be blacklisted: " + token);
            });
        }
    }
}