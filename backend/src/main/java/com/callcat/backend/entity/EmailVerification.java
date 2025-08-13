package com.callcat.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {
    
    @Id
    private String email;
    
    @Column(name = "verification_code", nullable = false)
    private String verificationCode;
    
    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "created_at", nullable = false)
    private Long createdAt = System.currentTimeMillis() / 1000;
    
    // Constructors
    public EmailVerification() {}
    
    public EmailVerification(String email, String verificationCode, Long expiresAt) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.createdAt = System.currentTimeMillis() / 1000;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    
    public Long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Check if the verification code has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() / 1000 > expiresAt;
    }
}