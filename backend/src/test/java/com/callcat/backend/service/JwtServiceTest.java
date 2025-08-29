package com.callcat.backend.service;

import com.callcat.backend.TestBackendApplication;
import com.callcat.backend.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Integration tests for JWT token service functionality
// Tests JWT token generation, parsing, validation, and security
// Uses Spring Boot context to properly initialize JWT service with test configuration
@SpringBootTest
@ActiveProfiles("test")
@Import(TestBackendApplication.class)
@TestPropertySource(properties = {
        "jwt.secret=NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU=",
        "jwt.expiration=86400000"
})
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Use reflection to set private fields for testing
        try {
            var secretField = JwtService.class.getDeclaredField("secretKey");
            secretField.setAccessible(true);
            secretField.set(jwtService, "NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU1NTU=");
            
            var expirationField = JwtService.class.getDeclaredField("jwtExpiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtService, 86400000L);
        } catch (Exception e) {
            fail("Failed to setup test: " + e.getMessage());
        }

        testUser = createTestUser();
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("encodedPassword");
        user.setIsActive(true);
        return user;
    }

    // Tests JWT token generation using UserDetails object
    // Verifies that tokens are properly created with user information embedded
    @Test
    void generateToken_WithUserDetails_ShouldReturnValidToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    // Tests JWT token generation with custom claims (userId, email, fullName)
    // Verifies that custom user data is properly embedded in token payload
    @Test
    void generateToken_WithCustomClaims_ShouldReturnValidToken() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        String fullName = "John Doe";

        // Act
        String token = jwtService.generateToken(email, userId, fullName);

        // Assert
        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(fullName, jwtService.extractFullName(token));
    }

    // Tests JWT token generation with additional custom claims
    // Verifies that extra metadata (role, department) can be added to tokens
    @Test
    void generateToken_WithExtraClaims_ShouldReturnValidToken() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("department", "IT");

        // Act
        String token = jwtService.generateToken(extraClaims, testUser);

        // Assert
        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    // Tests extraction of username (email) from JWT token
    // Verifies that the subject claim can be properly parsed from tokens
    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", username);
    }

    // Tests extraction of user ID from JWT token custom claims
    // Verifies that custom userId claim is properly stored and retrievable
    @Test
    void extractUserId_WithValidToken_ShouldReturnUserId() {
        // Arrange
        String token = jwtService.generateToken("test@example.com", 1L, "John Doe");

        // Act
        Long userId = jwtService.extractUserId(token);

        // Assert
        assertEquals(1L, userId);
    }

    // Tests extraction of full name from JWT token custom claims
    // Verifies that user display name is properly stored and retrievable
    @Test
    void extractFullName_WithValidToken_ShouldReturnFullName() {
        // Arrange
        String token = jwtService.generateToken("test@example.com", 1L, "John Doe");

        // Act
        String fullName = jwtService.extractFullName(token);

        // Assert
        assertEquals("John Doe", fullName);
    }

    // Tests JWT token validation against specific user
    // Verifies that tokens are valid for the user they were issued to
    @Test
    void isTokenValid_WithValidTokenAndUser_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    // Tests JWT token signature and expiration validation
    // Verifies that tokens are structurally valid and not expired
    @Test
    void isTokenValid_WithValidTokenOnly_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    // Tests JWT validation with malformed or corrupted tokens
    // Ensures that invalid token formats are properly rejected
    @Test
    void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    // Tests JWT token validation against wrong user
    // Ensures that tokens cannot be used by users they weren't issued to
    @Test
    void isTokenValid_WithWrongUser_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        
        User differentUser = new User();
        differentUser.setEmail("different@example.com");
        differentUser.setPassword("password");
        differentUser.setFirstName("Jane");
        differentUser.setLastName("Smith");
        differentUser.setIsActive(true);

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    // Tests JWT validation with null token input
    // Ensures that null safety is properly handled in token validation
    @Test
    void isTokenValid_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid(null);

        // Assert
        assertFalse(isValid);
    }

    // Tests JWT validation with empty string token
    // Ensures that empty tokens are properly rejected
    @Test
    void isTokenValid_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid("");

        // Assert
        assertFalse(isValid);
    }

    // Tests generic claim extraction from JWT tokens
    // Verifies that any claim can be extracted using custom claim resolver
    @Test
    void extractClaim_WithValidToken_ShouldReturnClaim() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, claims -> claims.getSubject());

        // Assert
        assertEquals("test@example.com", subject);
    }

    // Tests error handling for claim extraction from invalid tokens
    // Ensures that malformed tokens throw proper JWT exceptions
    @Test
    void extractClaim_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.extractClaim(invalidToken, claims -> claims.getSubject());
        });
    }

    // Tests JWT expiration time configuration
    // Verifies that the configured token lifetime is properly returned
    @Test
    void getExpirationTime_ShouldReturnConfiguredValue() {
        // Act
        long expirationTime = jwtService.getExpirationTime();

        // Assert
        assertEquals(86400000L, expirationTime);
    }

    // Tests that newly generated tokens are immediately valid
    // Ensures that tokens don't expire instantly due to timing issues
    @Test
    void token_ShouldNotBeExpiredImmediately() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }
}