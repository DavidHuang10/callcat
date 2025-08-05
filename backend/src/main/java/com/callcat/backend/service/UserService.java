package com.callcat.backend.service;

import com.callcat.backend.dto.UpdatePreferencesRequest;
import com.callcat.backend.dto.UpdateProfileRequest;
import com.callcat.backend.dto.UserPreferencesResponse;
import com.callcat.backend.dto.UserResponse;
import com.callcat.backend.entity.User;
import com.callcat.backend.entity.UserPreferences;
import com.callcat.backend.repository.UserPreferencesRepository;
import com.callcat.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Password pattern: 8+ chars, at least one uppercase, one lowercase, one digit
    private static final String PASSWORD_PATTERN = 
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);
    
    public UserService(
            UserRepository userRepository,
            UserPreferencesRepository userPreferencesRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
    
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        // Email cannot be changed - it's used as the unique identifier
        
        User updatedUser = userRepository.save(user);
        
        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName()
        );
    }
    
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
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
        userRepository.save(user);
    }
    
    public UserPreferencesResponse getUserPreferences(String email) {
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferences preferences = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));
        
        return new UserPreferencesResponse(
                preferences.getTimezone(),
                preferences.getEmailNotifications(),
                preferences.getVoiceId(),
                preferences.getSystemPrompt()
        );
    }
    
    @Transactional
    public UserPreferencesResponse updateUserPreferences(String email, UpdatePreferencesRequest request) {
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserPreferences preferences = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));
        
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
        
        UserPreferences updatedPreferences = userPreferencesRepository.save(preferences);
        
        return new UserPreferencesResponse(
                updatedPreferences.getTimezone(),
                updatedPreferences.getEmailNotifications(),
                updatedPreferences.getVoiceId(),
                updatedPreferences.getSystemPrompt()
        );
    }
    
    private UserPreferences createDefaultPreferences(User user) {
        UserPreferences preferences = new UserPreferences(user);
        return userPreferencesRepository.save(preferences);
    }
    
    private boolean isValidPassword(String password) {
        return passwordPattern.matcher(password).matches();
    }
}