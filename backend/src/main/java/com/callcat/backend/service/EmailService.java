package com.callcat.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    // Using Spring Security's KeyGenerators for all token generation
    
    /**
     * Generates a 6-digit verification code using Spring Security's secure random
     */
    public String generateVerificationCode() {
        // Generate secure random bytes and convert to 6-digit code
        byte[] randomBytes = KeyGenerators.secureRandom(4).generateKey();
        int randomInt = Math.abs(java.nio.ByteBuffer.wrap(randomBytes).getInt());
        return String.format("%06d", randomInt % 1000000);
    }
    
    /**
     * Generates a secure token for password reset (32 chars, hex-encoded)
     * Uses Spring Security's KeyGenerators for cryptographic strength
     */
    public String generateResetToken() {
        return KeyGenerators.string().generateKey();
    }
    
    /**
     * Sends email verification code to user
     * For now, just logs to console. In production, integrate with email provider.
     */
    public void sendVerificationEmail(String email, String code) {
        if (emailEnabled) {
            // TODO: Integrate with actual email service (SendGrid, SES, etc.)
            System.out.println("📧 Sending verification email to: " + email);
            System.out.println("🔢 Verification code: " + code);
        } else {
            // Development mode - log to console
            System.out.println("=== EMAIL VERIFICATION (DEV MODE) ===");
            System.out.println("To: " + email);
            System.out.println("Subject: Verify your CallCat account");
            System.out.println("Your verification code is: " + code);
            System.out.println("This code expires in 15 minutes.");
            System.out.println("=====================================");
        }
    }
    
    /**
     * Sends password reset email to user
     */
    public void sendPasswordResetEmail(String email, String token) {
        if (emailEnabled) {
            // TODO: Integrate with actual email service
            System.out.println("📧 Sending password reset email to: " + email);
            System.out.println("🔑 Reset token: " + token);
        } else {
            // Development mode - log to console
            System.out.println("=== PASSWORD RESET (DEV MODE) ===");
            System.out.println("To: " + email);
            System.out.println("Subject: Reset your CallCat password");
            System.out.println("Your password reset token is: " + token);
            System.out.println("This token expires in 1 hour.");
            System.out.println("Use this token to reset your password.");
            System.out.println("=================================");
        }
    }
    
    /**
     * In production, you would integrate with email services like:
     * - SendGrid: https://sendgrid.com/
     * - Amazon SES: https://aws.amazon.com/ses/
     * - Mailgun: https://www.mailgun.com/
     * - Postmark: https://postmarkapp.com/
     * 
     * For now, this service logs to console for development.
     */
}