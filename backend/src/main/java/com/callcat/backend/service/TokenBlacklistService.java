package com.callcat.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service to manage blacklisted JWT tokens for logout functionality.
 * 
 * TODO: Replace in-memory storage with Redis or similar NoSQL solution
 * with TTL of 24 hours (matching JWT expiration)
 * 
 * Current implementation uses ConcurrentHashMap for thread-safe in-memory storage.
 * In production, use Redis with automatic TTL cleanup.
 */
@Service
public class TokenBlacklistService {
    
    // In-memory storage for blacklisted tokens
    // Key: JWT token, Value: Expiration timestamp
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    /**
     * Adds a token to the blacklist
     * @param token The JWT token to blacklist
     */
    public void blacklistToken(String token) {
        // Set expiration to 24 hours from now (matching JWT expiration)
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);
        blacklistedTokens.put(token, expiration);
        
        // TODO: Replace with Redis implementation
        // redisTemplate.opsForValue().set("blacklist:" + token, "1", Duration.ofHours(24));
    }
    
    /**
     * Checks if a token is blacklisted
     * @param token The JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        LocalDateTime expiration = blacklistedTokens.get(token);
        if (expiration == null) {
            return false;
        }
        
        // Remove expired tokens during check
        if (LocalDateTime.now().isAfter(expiration)) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Cleans up expired tokens from the blacklist
     * Should be called periodically in production
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
    
    /**
     * Gets the current size of the blacklist (for monitoring)
     * @return Number of blacklisted tokens
     */
    public int getBlacklistSize() {
        cleanupExpiredTokens(); // Clean up before returning size
        return blacklistedTokens.size();
    }
    
    /**
     * Removes a token from the blacklist (for testing/admin purposes)
     * @param token The JWT token to remove from blacklist
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }
} 