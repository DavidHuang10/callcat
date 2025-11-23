package com.callcat.backend.service;

import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Unit tests for password reset functionality in AuthenticationService
// Tests forgot password and reset password flows with comprehensive edge cases
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepositoryDynamoDb userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserDynamoDb testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserDynamoDb();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedOldPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIsActive(true);
    }

    // Tests successful forgot password flow
    // Verifies complete flow: user lookup, token generation, storage, email sending
    @Test
    void forgotPassword_WithValidEmail_ShouldSendResetEmail() {
        // Arrange
        String email = "test@example.com";
        String resetToken = "secure-reset-token-123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(emailService.generateResetToken()).thenReturn(resetToken);

        // Act
        authenticationService.forgotPassword(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(emailService).generateResetToken();
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordResetEmail(email, resetToken);
        
        assertEquals(resetToken, testUser.getPasswordResetToken());
        assertNotNull(testUser.getResetTokenExpires());
        assertTrue(testUser.getResetTokenExpires() > System.currentTimeMillis() / 1000);
    }

    // Tests forgot password with non-existent email
    // Ensures that password reset cannot be initiated for non-registered emails
    @Test
    void forgotPassword_WithNonExistentEmail_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.forgotPassword(email));

        assertEquals("No account found with this email address", exception.getMessage());
        verify(emailService, never()).generateResetToken();
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // Tests forgot password with inactive user account
    // Ensures that deactivated accounts cannot reset passwords
    @Test
    void forgotPassword_WithInactiveUser_ShouldThrowException() {
        // Arrange
        String email = "inactive@example.com";
        UserDynamoDb inactiveUser = new UserDynamoDb();
        inactiveUser.setEmail(email);
        inactiveUser.setIsActive(false);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.forgotPassword(email));

        assertEquals("User is inactive", exception.getMessage());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // Tests successful password reset with valid token
    // Verifies complete reset flow: token validation, password update, token cleanup
    @Test
    void resetPassword_WithValidToken_ShouldUpdatePassword() {
        // Arrange
        String resetToken = "valid-reset-token";
        String newPassword = "NewStrongPass123";
        String encodedNewPassword = "encodedNewPassword";
        
        testUser.setPasswordResetToken(resetToken);
        testUser.setResetTokenExpires(System.currentTimeMillis() / 1000 + 3600);
        
        when(userRepository.findByPasswordResetToken(resetToken)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // Act
        authenticationService.resetPassword(resetToken, newPassword);

        // Assert
        verify(userRepository).findByPasswordResetToken(resetToken);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
        
        assertEquals(encodedNewPassword, testUser.getPassword());
        assertNull(testUser.getPasswordResetToken());
        assertNull(testUser.getResetTokenExpires());
    }

    // Tests password reset with invalid token
    // Ensures that non-existent tokens are properly rejected
    @Test
    void resetPassword_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid-token";
        String newPassword = "NewStrongPass123";
        
        when(userRepository.findByPasswordResetToken(invalidToken)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.resetPassword(invalidToken, newPassword));

        assertEquals("Invalid reset token", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests password reset with expired token
    // Ensures that expired tokens are rejected and cleaned up
    @Test
    void resetPassword_WithExpiredToken_ShouldThrowExceptionAndCleanupToken() {
        // Arrange
        String expiredToken = "expired-token";
        String newPassword = "NewStrongPass123";
        
        testUser.setPasswordResetToken(expiredToken);
        testUser.setResetTokenExpires(System.currentTimeMillis() / 1000 - 3600); // Expired 1 hour ago
        
        when(userRepository.findByPasswordResetToken(expiredToken)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.resetPassword(expiredToken, newPassword));

        assertEquals("Reset token has expired", exception.getMessage());
        verify(userRepository).save(testUser);
        
        // Verify token was cleaned up
        assertNull(testUser.getPasswordResetToken());
        assertNull(testUser.getResetTokenExpires());
        
        // Password should not be changed
        assertEquals("encodedOldPassword", testUser.getPassword());
    }

    // Tests password reset with token that has null expiration
    // Handles edge case where expiration date is missing
    @Test
    void resetPassword_WithTokenWithoutExpiration_ShouldThrowException() {
        // Arrange
        String token = "token-without-expiration";
        String newPassword = "NewStrongPass123";
        
        testUser.setPasswordResetToken(token);
        testUser.setResetTokenExpires(null); // No expiration set
        
        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.resetPassword(token, newPassword));

        assertEquals("Reset token has expired", exception.getMessage());
        verify(userRepository).save(testUser);
        assertNull(testUser.getPasswordResetToken());
    }

    // Tests password reset with weak password
    // Ensures password strength validation is applied during reset
    @Test
    void resetPassword_WithWeakPassword_ShouldThrowException() {
        // Arrange
        String resetToken = "valid-reset-token";
        String weakPassword = "weak"; // Doesn't meet strength requirements
        
        testUser.setPasswordResetToken(resetToken);
        testUser.setResetTokenExpires(System.currentTimeMillis() / 1000 + 3600);
        
        // Act & Assert - No stubbing needed since validation fails before repository lookup
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.resetPassword(resetToken, weakPassword));

        assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
        verify(passwordEncoder, never()).encode(anyString());
        
        // Token should not be cleared if password validation fails
        assertEquals(resetToken, testUser.getPasswordResetToken());
        assertEquals("encodedOldPassword", testUser.getPassword());
    }

    // Tests password reset with various invalid password formats
    // Comprehensive validation of password strength requirements
    @Test
    void resetPassword_WithInvalidPasswords_ShouldThrowException() {
        // Arrange
        String resetToken = "valid-reset-token";
        testUser.setPasswordResetToken(resetToken);
        testUser.setResetTokenExpires(System.currentTimeMillis() / 1000 + 3600);

        // Test various invalid passwords
        String[] invalidPasswords = {
            "short",           // Too short
            "nouppercase123",  // No uppercase
            "NOLOWERCASE123",  // No lowercase  
            "NoNumbers",       // No numbers
            "12345678"         // Only numbers
        };

        for (String invalidPassword : invalidPasswords) {
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authenticationService.resetPassword(resetToken, invalidPassword));

            assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
        }

        verify(passwordEncoder, never()).encode(anyString());
    }

    // Tests that reset token expiration is set to 1 hour from creation
    // Verifies proper token expiration timing
    @Test
    void forgotPassword_ShouldSetTokenExpirationToOneHour() {
        // Arrange
        String email = "test@example.com";
        String resetToken = "test-token";
        long beforeCall = System.currentTimeMillis() / 1000;
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(emailService.generateResetToken()).thenReturn(resetToken);

        // Act
        authenticationService.forgotPassword(email);

        // Assert
        long afterCall = System.currentTimeMillis() / 1000;
        long expectedExpiry = beforeCall + 3600; // 1 hour
        Long actualExpiry = testUser.getResetTokenExpires();
        
        assertNotNull(actualExpiry);
        assertTrue(actualExpiry >= expectedExpiry - 5); // Allow 5 second buffer
        assertTrue(actualExpiry <= afterCall + 3600 + 5);
    }
}