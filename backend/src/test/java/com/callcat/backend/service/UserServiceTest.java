package com.callcat.backend.service;

import com.callcat.backend.dto.UpdatePreferencesRequest;
import com.callcat.backend.dto.UpdateProfileRequest;
import com.callcat.backend.dto.UserPreferencesResponse;
import com.callcat.backend.dto.UserResponse;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.entity.dynamo.UserPreferencesDynamoDb;
import com.callcat.backend.repository.dynamo.UserPreferencesRepositoryDynamoDb;
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

// Comprehensive unit tests for UserService business logic
// Tests user profile management, password changes, and preferences handling
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryDynamoDb userRepository;

    @Mock
    private UserPreferencesRepositoryDynamoDb userPreferencesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDynamoDb testUser;
    private UserPreferencesDynamoDb testPreferences;

    @BeforeEach
    void setUp() {
        testUser = new UserDynamoDb();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);

        testPreferences = new UserPreferencesDynamoDb();
        testPreferences.setEmail(testUser.getEmail());
        testPreferences.setTimezone("UTC");
        testPreferences.setEmailNotifications(true);
        testPreferences.setVoiceId("voice123");
        testPreferences.setSystemPrompt("Be helpful and concise");
    }

    // Tests successful user profile retrieval
    // Verifies that active users can retrieve their profile information
    @Test
    void getUserProfile_WithValidUser_ShouldReturnUserResponse() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getUserProfile(email);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository).findByEmail(email);
    }

    // Tests user profile retrieval failure for non-existent user
    // Ensures proper error handling when user is not found
    @Test
    void getUserProfile_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserProfile(email));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(email);
    }

    // Tests successful profile update
    // Verifies that user names can be updated while preserving email
    @Test
    void updateProfile_WithValidData_ShouldUpdateAndReturnProfile() {
        // Arrange
        String email = "test@example.com";
        UpdateProfileRequest request = new UpdateProfileRequest("Jane", "Smith");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        // save is void, so we don't mock return value
        
        // Act
        UserResponse result = userService.updateProfile(email, request);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("test@example.com", result.getEmail()); // Email should remain unchanged
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testUser);
        
        // Verify user object was updated
        assertEquals("Jane", testUser.getFirstName());
        assertEquals("Smith", testUser.getLastName());
    }

    // Tests profile update failure for non-existent user
    // Ensures proper error handling during profile updates
    @Test
    void updateProfile_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        UpdateProfileRequest request = new UpdateProfileRequest("Jane", "Smith");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateProfile(email, request));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests successful password change with correct current password
    // Verifies complete password change flow: validation, encoding, storage
    @Test
    void changePassword_WithCorrectCurrentPassword_ShouldUpdatePassword() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "oldPassword";
        String newPassword = "NewStrongPass123";
        String encodedNewPassword = "encodedNewPassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // Act
        userService.changePassword(email, currentPassword, newPassword);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, "encodedPassword"); // Original password
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
        assertEquals(encodedNewPassword, testUser.getPassword());
    }

    // Tests password change failure with incorrect current password
    // Ensures that password changes require current password verification
    @Test
    void changePassword_WithIncorrectCurrentPassword_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String wrongCurrentPassword = "wrongPassword";
        String newPassword = "NewStrongPass123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongCurrentPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword(email, wrongCurrentPassword, newPassword));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests password change failure with weak new password
    // Ensures new passwords meet strength requirements
    @Test
    void changePassword_WithWeakNewPassword_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "oldPassword";
        String weakNewPassword = "weak"; // Doesn't meet requirements
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword(email, currentPassword, weakNewPassword));

        assertTrue(exception.getMessage().contains("New password must be at least 8 characters"));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests password change for non-existent user
    // Ensures proper error handling when user is not found
    @Test
    void changePassword_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        String currentPassword = "password";
        String newPassword = "NewStrongPass123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.changePassword(email, currentPassword, newPassword));

        assertEquals("User not found", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // Tests retrieval of existing user preferences
    // Verifies that stored preferences are properly returned
    @Test
    void getUserPreferences_WithExistingPreferences_ShouldReturnPreferences() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userPreferencesRepository.findByEmail(email)).thenReturn(Optional.of(testPreferences));

        // Act
        UserPreferencesResponse result = userService.getUserPreferences(email);

        // Assert
        assertNotNull(result);
        assertEquals("UTC", result.getTimezone());
        assertEquals(true, result.getEmailNotifications());
        assertEquals("voice123", result.getVoiceId());
        assertEquals("Be helpful and concise", result.getSystemPrompt());
        verify(userRepository).findByEmail(email);
        verify(userPreferencesRepository).findByEmail(email);
    }

    // Tests retrieval of preferences for user without existing preferences
    // Verifies that default preferences are created and returned
    @Test
    void getUserPreferences_WithoutExistingPreferences_ShouldCreateAndReturnDefaults() {
        // Arrange
        String email = "test@example.com";
        UserPreferencesDynamoDb defaultPrefs = new UserPreferencesDynamoDb();
        defaultPrefs.setEmail(testUser.getEmail());
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userPreferencesRepository.findByEmail(email)).thenReturn(Optional.empty());
        // save is void

        // Act
        UserPreferencesResponse result = userService.getUserPreferences(email);

        // Assert
        assertNotNull(result);
        verify(userRepository).findByEmail(email);
        verify(userPreferencesRepository).findByEmail(email);
        verify(userPreferencesRepository).save(any(UserPreferencesDynamoDb.class));
    }

    // Tests preferences retrieval for non-existent user
    // Ensures proper error handling when user is not found
    @Test
    void getUserPreferences_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserPreferences(email));

        assertEquals("User not found", exception.getMessage());
        verify(userPreferencesRepository, never()).findByEmail(any());
    }

    // Tests comprehensive preferences update with all fields
    // Verifies that all preference fields can be updated simultaneously
    @Test
    void updateUserPreferences_WithAllFields_ShouldUpdateAllPreferences() {
        // Arrange
        String email = "test@example.com";
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "America/New_York", false, "voice456", "Be more formal"
        );
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userPreferencesRepository.findByEmail(email)).thenReturn(Optional.of(testPreferences));
        // save is void

        // Act
        UserPreferencesResponse result = userService.updateUserPreferences(email, request);

        // Assert
        assertNotNull(result);
        assertEquals("America/New_York", result.getTimezone());
        assertEquals(false, result.getEmailNotifications());
        assertEquals("voice456", result.getVoiceId());
        assertEquals("Be more formal", result.getSystemPrompt());
        
        verify(userRepository).findByEmail(email);
        verify(userPreferencesRepository).findByEmail(email);
        verify(userPreferencesRepository).save(testPreferences);
        
        // Verify preferences object was updated
        assertEquals("America/New_York", testPreferences.getTimezone());
        assertEquals(false, testPreferences.getEmailNotifications());
        assertEquals("voice456", testPreferences.getVoiceId());
        assertEquals("Be more formal", testPreferences.getSystemPrompt());
    }

    // Tests partial preferences update with only some fields
    // Verifies that null fields are ignored and existing values preserved
    @Test
    void updateUserPreferences_WithPartialFields_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        String email = "test@example.com";
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "Europe/London", null, null, "Updated prompt only"
        );
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userPreferencesRepository.findByEmail(email)).thenReturn(Optional.of(testPreferences));
        // save is void

        // Act
        UserPreferencesResponse result = userService.updateUserPreferences(email, request);

        // Assert
        assertNotNull(result);
        assertEquals("Europe/London", result.getTimezone()); // Updated
        assertEquals(true, result.getEmailNotifications()); // Preserved
        assertEquals("voice123", result.getVoiceId()); // Preserved
        assertEquals("Updated prompt only", result.getSystemPrompt()); // Updated
        
        verify(userPreferencesRepository).save(testPreferences);
    }

    // Tests preferences update for user without existing preferences
    // Verifies that default preferences are created before updating
    @Test
    void updateUserPreferences_WithoutExistingPreferences_ShouldCreateAndUpdate() {
        // Arrange
        String email = "test@example.com";
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "Asia/Tokyo", true, "voice789", "Custom prompt"
        );
        UserPreferencesDynamoDb newPreferences = new UserPreferencesDynamoDb();
        newPreferences.setEmail(testUser.getEmail());
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userPreferencesRepository.findByEmail(email)).thenReturn(Optional.empty());
        // save is void

        // Act
        UserPreferencesResponse result = userService.updateUserPreferences(email, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findByEmail(email);
        verify(userPreferencesRepository).findByEmail(email);
        verify(userPreferencesRepository, times(2)).save(any(UserPreferencesDynamoDb.class)); // Create + Update
    }

    // Tests preferences update for non-existent user
    // Ensures proper error handling when user is not found
    @Test
    void updateUserPreferences_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "UTC", true, "voice123", "prompt"
        );
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUserPreferences(email, request));

        assertEquals("User not found", exception.getMessage());
        verify(userPreferencesRepository, never()).findByEmail(any());
        verify(userPreferencesRepository, never()).save(any());
    }

    // Tests password validation with various invalid formats
    // Comprehensive validation of password strength requirements
    @Test
    void changePassword_WithVariousInvalidPasswords_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "validCurrent";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

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
                    () -> userService.changePassword(email, currentPassword, invalidPassword));

            assertTrue(exception.getMessage().contains("New password must be at least 8 characters"));
        }

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests password validation with valid formats
    // Ensures that strong passwords are accepted
    @Test
    void changePassword_WithValidPasswords_ShouldSucceed() {
        // Arrange
        String email = "test@example.com";
        String currentPassword = "validCurrent";
        String[] validPasswords = {
            "StrongPass123",
            "MyP@ssw0rd",
            "ComplexPassword1",
            "Secure123Pass"
        };
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        for (String validPassword : validPasswords) {
            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> userService.changePassword(email, currentPassword, validPassword));
        }

        verify(passwordEncoder, times(validPasswords.length)).encode(anyString());
        verify(userRepository, times(validPasswords.length)).save(testUser);
    }
}