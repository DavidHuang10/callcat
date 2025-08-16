package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.service.AuthenticationService;
import com.callcat.backend.service.TokenBlacklistService;
import com.callcat.backend.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final VerificationService verificationService;
    private final TokenBlacklistService tokenBlacklistService;
    
    public AuthController(AuthenticationService authenticationService, VerificationService verificationService, TokenBlacklistService tokenBlacklistService) {
        this.authenticationService = authenticationService;
        this.verificationService = verificationService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authenticationService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authenticationService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            tokenBlacklistService.blacklistToken(token);
            return ResponseEntity.ok(ApiResponse.success("Successfully logged out"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerification(@Valid @RequestBody EmailRequest request) {
        try {
            verificationService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Verification code sent to email"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            verificationService.verifyEmailCode(request.getEmail(), request.getCode());
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody EmailRequest request) {
        try {
            authenticationService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Password reset instructions sent to email"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}