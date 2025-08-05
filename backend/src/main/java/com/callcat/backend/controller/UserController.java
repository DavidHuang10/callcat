package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponse response = userService.getUserProfile(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication, 
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            String email = authentication.getName();
            UserResponse response = userService.updateProfile(email, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            String email = authentication.getName();
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse("Password changed successfully", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @GetMapping("/preferences")
    public ResponseEntity<?> getUserPreferences(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserPreferencesResponse response = userService.getUserPreferences(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @PutMapping("/preferences")
    public ResponseEntity<?> updateUserPreferences(
            Authentication authentication,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        try {
            String email = authentication.getName();
            UserPreferencesResponse response = userService.updateUserPreferences(email, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
}