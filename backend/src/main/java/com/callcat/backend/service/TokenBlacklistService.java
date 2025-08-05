package com.callcat.backend.service;

import com.callcat.backend.entity.BlacklistedToken;
import com.callcat.backend.repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service to manage blacklisted JWT tokens for logout functionality.
 * Uses DynamoDB with TTL for automatic cleanup.
 */
@Service
public class TokenBlacklistService {
    
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    
    @Autowired
    public TokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }
    
    /**
     * Adds a token to the blacklist
     * @param token The JWT token to blacklist
     */
    public void blacklistToken(String token) {
        // Set expiration to 24 hours from now (matching JWT expiration)
        long expiresAt = Instant.now().plusSeconds(86400).getEpochSecond(); // 24 hours
        
        BlacklistedToken blacklistedToken = new BlacklistedToken(token, expiresAt);
        blacklistedTokenRepository.save(blacklistedToken);
    }
    
    /**
     * Checks if a token is blacklisted
     * @param token The JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.exists(token);
    }
} 