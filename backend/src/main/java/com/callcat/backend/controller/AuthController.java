package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.entity.User;
import com.callcat.backend.service.AuthenticationService;
import com.callcat.backend.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    
    public AuthController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            String token = authenticationService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );
            
            // Extract user info from token for response
            String email = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            String fullName = jwtService.extractFullName(token);
            long expirationTime = jwtService.getExpirationTime();
            
            AuthResponse response = new AuthResponse(token, userId, email, fullName, expirationTime);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authenticationService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );
            
            // Extract user info from token for response
            String email = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            String fullName = jwtService.extractFullName(token);
            long expirationTime = jwtService.getExpirationTime();
            
            AuthResponse response = new AuthResponse(token, userId, email, fullName, expirationTime);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = authenticationService.getCurrentUser(email);
            
            UserResponse response = new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getCreatedAt()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody PasswordValidationRequest request) {
        boolean isValid = authenticationService.validatePasswordStrength(request.getPassword());
        return ResponseEntity.ok(new PasswordValidationResponse(isValid, 
            isValid ? "Password is strong" : "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number"));
    }
    
    // Inner classes for additional DTOs
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    public static class PasswordValidationRequest {
        private String password;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    public static class PasswordValidationResponse {
        private boolean valid;
        private String message;
        
        public PasswordValidationResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}