package com.callcat.backend.service;

import com.callcat.backend.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {
    
    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;
    
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(blacklistedTokenRepository);
    }
    
    @Test
    void blacklistToken_ShouldCallRepositorySave() {
        // Arrange
        String token = "test.jwt.token";
        
        // Act
        tokenBlacklistService.blacklistToken(token);
        
        // Assert
        verify(blacklistedTokenRepository, times(1)).save(any());
    }
    
    @Test
    void isTokenBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
        // Arrange
        String token = "test.jwt.token";
        when(blacklistedTokenRepository.exists(anyString())).thenReturn(false);
        
        // Act
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        
        // Assert
        assertFalse(isBlacklisted);
        verify(blacklistedTokenRepository, times(1)).exists(token);
    }
    
    @Test
    void isTokenBlacklisted_WithBlacklistedToken_ShouldReturnTrue() {
        // Arrange
        String token = "test.jwt.token";
        when(blacklistedTokenRepository.exists(anyString())).thenReturn(true);
        
        // Act
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        
        // Assert
        assertTrue(isBlacklisted);
        verify(blacklistedTokenRepository, times(1)).exists(token);
    }
} 