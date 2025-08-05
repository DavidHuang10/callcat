package com.callcat.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenBlacklistServiceTest {
    
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }
    
    @Test
    void blacklistToken_ShouldAddTokenToBlacklist() {
        // Arrange
        String token = "test.jwt.token";
        
        // Act
        tokenBlacklistService.blacklistToken(token);
        
        // Assert
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void isTokenBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
        // Arrange
        String token = "test.jwt.token";
        
        // Act
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        
        // Assert
        assertFalse(isBlacklisted);
    }
    
    @Test
    void isTokenBlacklisted_WithBlacklistedToken_ShouldReturnTrue() {
        // Arrange
        String token = "test.jwt.token";
        tokenBlacklistService.blacklistToken(token);
        
        // Act
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        
        // Assert
        assertTrue(isBlacklisted);
    }
    
    @Test
    void removeFromBlacklist_ShouldRemoveToken() {
        // Arrange
        String token = "test.jwt.token";
        tokenBlacklistService.blacklistToken(token);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        
        // Act
        tokenBlacklistService.removeFromBlacklist(token);
        
        // Assert
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void getBlacklistSize_ShouldReturnCorrectSize() {
        // Arrange
        String token1 = "test.jwt.token.1";
        String token2 = "test.jwt.token.2";
        
        // Act
        int initialSize = tokenBlacklistService.getBlacklistSize();
        tokenBlacklistService.blacklistToken(token1);
        int sizeAfterOne = tokenBlacklistService.getBlacklistSize();
        tokenBlacklistService.blacklistToken(token2);
        int sizeAfterTwo = tokenBlacklistService.getBlacklistSize();
        
        // Assert
        assertEquals(0, initialSize);
        assertEquals(1, sizeAfterOne);
        assertEquals(2, sizeAfterTwo);
    }
} 