package com.callcat.backend.service;

import com.callcat.backend.dto.UpdatePreferencesRequest;
import com.callcat.backend.dto.UpdateProfileRequest;
import com.callcat.backend.dto.UserPreferencesResponse;
import com.callcat.backend.dto.UserResponse;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.entity.dynamo.UserPreferencesDynamoDb;
import com.callcat.backend.repository.dynamo.UserPreferencesRepositoryDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {
    
    private final UserRepositoryDynamoDb userRepository;
    private final UserPreferencesRepositoryDynamoDb userPreferencesRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Password pattern: 8+ chars, at least one uppercase, one lowercase, one digit
    private static final String PASSWORD_PATTERN = 
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);
    
    public UserService(
            UserRepositoryDynamoDb userRepository,
            UserPreferencesRepositoryDynamoDb userPreferencesRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserResponse getUserProfile(String email) {
        String lowerCaseEmail = email.toLowerCase();
        UserDynamoDb user = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!Boolean.TRUE.equals(user.getIsActive())) {
             throw new RuntimeException("User is inactive");
        }
        
        return new UserResponse(
                null, // ID is not available/used
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
    
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        String lowerCaseEmail = email.toLowerCase();
        UserDynamoDb user = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!Boolean.TRUE.equals(user.getIsActive())) {
             throw new RuntimeException("User is inactive");
        }
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdatedAt(java.time.LocalDateTime.now().toString());
        // Email cannot be changed - it's used as the unique identifier
        
        userRepository.save(user);
        
        return new UserResponse(
                null,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
    
    public void changePassword(String email, String currentPassword, String newPassword) {
        String lowerCaseEmail = email.toLowerCase();
        UserDynamoDb user = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!Boolean.TRUE.equals(user.getIsActive())) {
             throw new RuntimeException("User is inactive");
        }
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password strength
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("New password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(java.time.LocalDateTime.now().toString());
        userRepository.save(user);
    }
    
    public UserPreferencesResponse getUserPreferences(String email) {
        String lowerCaseEmail = email.toLowerCase();
        // Verify user exists first
        UserDynamoDb user = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferencesDynamoDb preferences = userPreferencesRepository.findByEmail(lowerCaseEmail)
                .orElseGet(() -> createDefaultPreferences(lowerCaseEmail));
        
        return new UserPreferencesResponse(
                preferences.getTimezone(),
                preferences.getEmailNotifications(),
                preferences.getVoiceId(),
                preferences.getSystemPrompt()
        );
    }
    
    public UserPreferencesResponse updateUserPreferences(String email, UpdatePreferencesRequest request) {
        String lowerCaseEmail = email.toLowerCase();
        // Verify user exists
        UserDynamoDb user = userRepository.findByEmail(lowerCaseEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferencesDynamoDb preferences = userPreferencesRepository.findByEmail(lowerCaseEmail)
                .orElseGet(() -> createDefaultPreferences(lowerCaseEmail));
        
        // Update only non-null fields
        if (request.getTimezone() != null) {
            preferences.setTimezone(request.getTimezone());
        }
        if (request.getEmailNotifications() != null) {
            preferences.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getVoiceId() != null) {
            preferences.setVoiceId(request.getVoiceId());
        }
        if (request.getSystemPrompt() != null) {
            preferences.setSystemPrompt(request.getSystemPrompt());
        }
        
        userPreferencesRepository.save(preferences);
        
        return new UserPreferencesResponse(
                preferences.getTimezone(),
                preferences.getEmailNotifications(),
                preferences.getVoiceId(),
                preferences.getSystemPrompt()
        );
    }
    
    private UserPreferencesDynamoDb createDefaultPreferences(String email) {
        UserPreferencesDynamoDb preferences = new UserPreferencesDynamoDb();
        preferences.setEmail(email);
        userPreferencesRepository.save(preferences);
        return preferences;
    }
    
    private boolean isValidPassword(String password) {
        return passwordPattern.matcher(password).matches();
    }
}