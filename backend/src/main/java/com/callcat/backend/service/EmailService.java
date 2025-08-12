package com.callcat.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class EmailService {

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.from-name:CallCat}")
    private String fromName;
    
    private final SesClient sesClient;
    
    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }
    
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
     */
    public void sendVerificationEmail(String email, String code) {
        if (emailEnabled) {
            try {
                String subject = "Verify your CallCat account";
                String htmlBody = """
                    <html>
                    <body>
                        <h2>Welcome to CallCat!</h2>
                        <p>Please verify your email address using the code below:</p>
                        <div style="background-color: #f0f0f0; padding: 20px; text-align: center; margin: 20px 0;">
                            <h1 style="color: #333; font-family: monospace; letter-spacing: 3px;">%s</h1>
                        </div>
                        <p>This code expires in 15 minutes.</p>
                        <p>If you didn't create a CallCat account, please ignore this email.</p>
                        <br>
                        <p>Best regards,<br>The CallCat Team</p>
                    </body>
                    </html>
                    """.formatted(code);
                
                String textBody = """
                    Welcome to CallCat!
                    
                    Please verify your email address using this code: %s
                    
                    This code expires in 15 minutes.
                    
                    If you didn't create a CallCat account, please ignore this email.
                    
                    Best regards,
                    The CallCat Team
                    """.formatted(code);
                
                sendEmail(email, subject, htmlBody, textBody);
                System.out.println("✅ Verification email sent successfully to: " + email);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to send verification email to: " + email);
                System.err.println("Error: " + e.getMessage());
                // Fallback to console logging
                logEmailToConsole(email, "Verify your CallCat account", "Your verification code is: " + code);
            }
        } else {
            // Development mode - log to console
            logEmailToConsole(email, "Verify your CallCat account", "Your verification code is: " + code + "\nThis code expires in 15 minutes.");
        }
    }
    
    /**
     * Sends password reset email to user
     */
    public void sendPasswordResetEmail(String email, String token) {
        if (emailEnabled) {
            try {
                String subject = "Reset your CallCat password";
                String htmlBody = """
                    <html>
                    <body>
                        <h2>Password Reset Request</h2>
                        <p>We received a request to reset your CallCat account password.</p>
                        <p>Use the following token to reset your password:</p>
                        <div style="background-color: #f0f0f0; padding: 15px; text-align: center; margin: 20px 0; word-break: break-all;">
                            <code style="color: #333; font-size: 14px;">%s</code>
                        </div>
                        <p><strong>This token expires in 1 hour.</strong></p>
                        <p>If you didn't request a password reset, please ignore this email. Your password will remain unchanged.</p>
                        <br>
                        <p>Best regards,<br>The CallCat Team</p>
                    </body>
                    </html>
                    """.formatted(token);
                
                String textBody = """
                    Password Reset Request
                    
                    We received a request to reset your CallCat account password.
                    
                    Use the following token to reset your password:
                    %s
                    
                    This token expires in 1 hour.
                    
                    If you didn't request a password reset, please ignore this email.
                    
                    Best regards,
                    The CallCat Team
                    """.formatted(token);
                
                sendEmail(email, subject, htmlBody, textBody);
                System.out.println("✅ Password reset email sent successfully to: " + email);
                
            } catch (Exception e) {
                System.err.println("❌ Failed to send password reset email to: " + email);
                System.err.println("Error: " + e.getMessage());
                // Fallback to console logging
                logEmailToConsole(email, "Reset your CallCat password", "Your password reset token is: " + token);
            }
        } else {
            // Development mode - log to console
            logEmailToConsole(email, "Reset your CallCat password", "Your password reset token is: " + token + "\nThis token expires in 1 hour.");
        }
    }
    
    /**
     * Sends an email using AWS SES
     */
    private void sendEmail(String toEmail, String subject, String htmlBody, String textBody) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .source(fromName + " <" + fromEmail + ">")
                .destination(Destination.builder()
                    .toAddresses(toEmail)
                    .build())
                .message(Message.builder()
                    .subject(Content.builder()
                        .charset("UTF-8")
                        .data(subject)
                        .build())
                    .body(Body.builder()
                        .html(Content.builder()
                            .charset("UTF-8")
                            .data(htmlBody)
                            .build())
                        .text(Content.builder()
                            .charset("UTF-8")
                            .data(textBody)
                            .build())
                        .build())
                    .build())
                .build();
            
            SendEmailResponse response = sesClient.sendEmail(request);
            System.out.println("Email sent successfully. Message ID: " + response.messageId());
            
        } catch (Exception e) {
            System.err.println("Failed to send email via SES: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Fallback method to log email to console
     */
    private void logEmailToConsole(String email, String subject, String body) {
        System.out.println("=== EMAIL (DEV MODE) ===");
        System.out.println("To: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("========================");
    }
}