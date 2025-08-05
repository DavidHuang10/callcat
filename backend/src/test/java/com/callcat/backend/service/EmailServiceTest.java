package com.callcat.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for EmailService functionality
// Tests token generation and email sending behavior (console output in dev mode)
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        // Set email disabled for testing (dev mode)
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
    }

    // Tests verification code generation format and uniqueness
    // Verifies that codes are 6-digit strings and each generation produces different codes
    @Test
    void generateVerificationCode_ShouldReturnSixDigitString() {
        // Act
        String code1 = emailService.generateVerificationCode();
        String code2 = emailService.generateVerificationCode();

        // Assert
        assertNotNull(code1);
        assertNotNull(code2);
        assertEquals(6, code1.length());
        assertEquals(6, code2.length());
        assertTrue(code1.matches("\\d{6}"));
        assertTrue(code2.matches("\\d{6}"));
        // Very unlikely to be the same (1 in 1,000,000 chance)
        assertNotEquals(code1, code2);
    }

    // Tests reset token generation format and uniqueness
    // Verifies that tokens are alphanumeric strings and each generation produces different tokens
    @Test
    void generateResetToken_ShouldReturnAlphanumericString() {
        // Act
        String token1 = emailService.generateResetToken();
        String token2 = emailService.generateResetToken();

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(token1.length() >= 16); // Should be reasonably long (16 chars for hex string)
        assertTrue(token2.length() >= 16);
        assertTrue(token1.matches("[a-fA-F0-9]+"));
        assertTrue(token2.matches("[a-fA-F0-9]+"));
        assertNotEquals(token1, token2);
    }

    // Tests verification email sending (console output in dev mode)
    // This test ensures the method doesn't throw exceptions and handles the dev mode flow
    @Test
    void sendVerificationEmail_InDevMode_ShouldNotThrowException() {
        // Arrange
        String email = "test@example.com";
        String code = "123456";

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(email, code));
    }

    // Tests password reset email sending (console output in dev mode)
    // This test ensures the method doesn't throw exceptions and handles the dev mode flow
    @Test
    void sendPasswordResetEmail_InDevMode_ShouldNotThrowException() {
        // Arrange
        String email = "test@example.com";
        String token = "abc123def456";

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(email, token));
    }

    // Tests verification email sending with email enabled
    // Verifies behavior when actual email sending would be attempted
    @Test
    void sendVerificationEmail_WithEmailEnabled_ShouldNotThrowException() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        String email = "test@example.com";
        String code = "123456";

        // Act & Assert - Should not throw any exception even in "production" mode
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(email, code));
    }

    // Tests password reset email sending with email enabled
    // Verifies behavior when actual email sending would be attempted
    @Test
    void sendPasswordResetEmail_WithEmailEnabled_ShouldNotThrowException() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        String email = "test@example.com";
        String token = "abc123def456";

        // Act & Assert - Should not throw any exception even in "production" mode
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(email, token));
    }
}