package com.callcat.backend.service;

import com.callcat.backend.entity.EmailVerification;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.EmailVerificationRepository;
import com.callcat.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Unit tests for VerificationService business logic
// Tests email verification flow, code generation, validation, and cleanup
@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @InjectMocks
    private VerificationService verificationService;

    private User testUser;
    private EmailVerification testVerification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("existing@example.com");
        testUser.setIsActive(true);

        testVerification = new EmailVerification();
        testVerification.setEmail("test@example.com");
        testVerification.setVerificationCode("123456");
        testVerification.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        testVerification.setVerified(false);
    }

    // Tests successful verification code sending for new email
    // Verifies complete flow: validation, code generation, storage, email sending
    @Test
    void sendVerificationCode_WithValidNewEmail_ShouldSendCode() {
        // Arrange
        String email = "newuser@example.com";
        String code = "654321";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(emailVerificationRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(emailService.generateVerificationCode()).thenReturn(code);

        // Act
        verificationService.sendVerificationCode(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(emailVerificationRepository).findByEmail(email);
        verify(emailService).generateVerificationCode();
        verify(emailVerificationRepository).save(any(EmailVerification.class));
        verify(emailService).sendVerificationEmail(email, code);
    }

    // Tests verification code sending for email with existing verification
    // Verifies that existing records are updated instead of creating duplicates
    @Test
    void sendVerificationCode_WithExistingVerification_ShouldUpdateRecord() {
        // Arrange
        String email = "test@example.com";
        String newCode = "789012";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(emailVerificationRepository.findByEmail(email)).thenReturn(Optional.of(testVerification));
        when(emailService.generateVerificationCode()).thenReturn(newCode);

        // Act
        verificationService.sendVerificationCode(email);

        // Assert
        verify(emailVerificationRepository).save(testVerification);
        verify(emailService).sendVerificationEmail(email, newCode);
        assertEquals(newCode, testVerification.getVerificationCode());
        assertFalse(testVerification.getVerified()); // Should reset verification status
    }

    // Tests rejection of verification code sending for already registered email
    // Ensures that users cannot bypass registration by getting new verification codes
    @Test
    void sendVerificationCode_WithRegisteredEmail_ShouldThrowException() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationService.sendVerificationCode(email));

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    // Tests rejection of invalid email formats
    // Ensures basic email validation before processing verification requests
    @Test
    void sendVerificationCode_WithInvalidEmail_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationService.sendVerificationCode("invalid-email"));

        assertEquals("Invalid email format", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    // Tests successful email verification with valid code
    // Verifies complete verification flow: code validation, expiration check, marking as verified
    @Test
    void verifyEmailCode_WithValidCode_ShouldReturnTrue() {
        // Arrange
        String email = "test@example.com";
        String code = "123456";
        
        when(emailVerificationRepository.findByEmail(email)).thenReturn(Optional.of(testVerification));

        // Act
        boolean result = verificationService.verifyEmailCode(email, code);

        // Assert
        assertTrue(result);
        assertTrue(testVerification.getVerified());
        verify(emailVerificationRepository).save(testVerification);
    }

    // Tests email verification failure with wrong code
    // Ensures that incorrect verification codes are properly rejected
    @Test
    void verifyEmailCode_WithWrongCode_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String wrongCode = "wrong123";
        
        when(emailVerificationRepository.findByEmail(email)).thenReturn(Optional.of(testVerification));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationService.verifyEmailCode(email, wrongCode));

        assertEquals("Invalid verification code", exception.getMessage());
        assertFalse(testVerification.getVerified());
        verify(emailVerificationRepository, never()).save(any());
    }

    // Tests email verification failure with expired code
    // Ensures that expired verification codes cannot be used
    @Test
    void verifyEmailCode_WithExpiredCode_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String code = "123456";
        testVerification.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired 1 minute ago
        
        when(emailVerificationRepository.findByEmail(email)).thenReturn(Optional.of(testVerification));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationService.verifyEmailCode(email, code));

        assertEquals("Verification code expired", exception.getMessage());
        assertFalse(testVerification.getVerified());
    }

    // Tests email verification attempt for non-existent verification record
    // Ensures proper error handling when no verification was ever sent
    @Test
    void verifyEmailCode_WithoutVerificationRecord_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        String code = "123456";
        
        when(emailVerificationRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verificationService.verifyEmailCode(email, code));

        assertEquals("No verification found for this email", exception.getMessage());
    }

    // Tests email verification status check for verified email
    // Verifies that properly verified emails are recognized as valid
    @Test
    void isEmailVerified_WithVerifiedEmail_ShouldReturnTrue() {
        // Arrange
        String email = "test@example.com";
        testVerification.setVerified(true);
        
        when(emailVerificationRepository.findVerifiedByEmail(email)).thenReturn(Optional.of(testVerification));

        // Act
        boolean result = verificationService.isEmailVerified(email);

        // Assert
        assertTrue(result);
    }

    // Tests email verification status check for unverified email
    // Ensures that unverified emails are properly identified
    @Test
    void isEmailVerified_WithUnverifiedEmail_ShouldReturnFalse() {
        // Arrange
        String email = "test@example.com";
        
        when(emailVerificationRepository.findVerifiedByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = verificationService.isEmailVerified(email);

        // Assert
        assertFalse(result);
    }

    // Tests email verification status check for expired verification
    // Ensures that expired verifications are treated as invalid
    @Test
    void isEmailVerified_WithExpiredVerification_ShouldReturnFalse() {
        // Arrange
        String email = "test@example.com";
        testVerification.setVerified(true);
        testVerification.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired
        
        when(emailVerificationRepository.findVerifiedByEmail(email)).thenReturn(Optional.of(testVerification));

        // Act
        boolean result = verificationService.isEmailVerified(email);

        // Assert
        assertFalse(result);
    }

    // Tests cleanup of expired verification records
    // Verifies that maintenance operations work correctly
    @Test
    void cleanupExpiredVerifications_ShouldCallRepositoryDeleteMethod() {
        // Act
        verificationService.cleanupExpiredVerifications();

        // Assert
        verify(emailVerificationRepository).deleteExpiredVerifications(any(LocalDateTime.class));
    }
}