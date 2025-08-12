package com.callcat.backend.service;

import com.callcat.backend.entity.EmailVerification;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.EmailVerificationRepository;
import com.callcat.backend.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    
    public VerificationService(UserRepository userRepository, EmailService emailService, EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.emailVerificationRepository = emailVerificationRepository;
    }
    
    /**
     * Sends verification code to email. Clean approach - no user creation until registration.
     */
    public void sendVerificationCode(String email) {
        // Validate email format first
        if (!isValidEmail(email)) {
            throw new RuntimeException("Invalid email format");
        }
        
        // Check if email already registered
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        // Generate verification code and expiration (15 minutes)
        String code = emailService.generateVerificationCode();
        LocalDateTime expires = LocalDateTime.now().plusMinutes(15);
        
        // Create or update email verification record
        Optional<EmailVerification> existingVerification = emailVerificationRepository.findByEmail(email);
        EmailVerification verification;
        
        if (existingVerification.isPresent()) {
            // Update existing verification
            verification = existingVerification.get();
            verification.setVerificationCode(code);
            verification.setExpiresAt(expires);
            verification.setVerified(false); // Reset verification status
        } else {
            // Create new verification record
            verification = new EmailVerification(email, code, expires);
        }
        
        emailVerificationRepository.save(verification);
        
        // Send email
        emailService.sendVerificationEmail(email, code);
    }
    
    /**
     * Verifies the email code and marks email as verified
     */
    public boolean verifyEmailCode(String email, String code) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByEmail(email);
        if (verificationOpt.isEmpty()) {
            throw new RuntimeException("No verification found for this email");
        }
        
        EmailVerification verification = verificationOpt.get();
        
        // Check if code matches
        if (!code.equals(verification.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }
        
        // Check if code hasn't expired
        if (verification.isExpired()) {
            throw new RuntimeException("Verification code expired");
        }
        
        // Mark email as verified
        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        
        return true;
    }
    
    /**
     * Checks if email is verified and ready for registration
     */
    public boolean isEmailVerified(String email) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findVerifiedByEmail(email);
        return verificationOpt.isPresent() && !verificationOpt.get().isExpired();
    }
    
    /**
     * Clean up expired verification records (should be called periodically)
     */
    public void cleanupExpiredVerifications() {
        emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
    }
    
    /**
     * Scheduled cleanup task - runs every hour to remove expired verification records
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0 second 0
    @Transactional
    public void scheduledCleanup() {
        try {
            emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
            System.out.println("Cleaned up expired email verifications at " + LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Failed to cleanup expired verifications: " + e.getMessage());
        }
    }
    
    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}