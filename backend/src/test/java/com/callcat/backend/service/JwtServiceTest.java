package com.callcat.backend.service;

import com.callcat.backend.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
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
            secretField.set(jwtService, "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
            
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

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    void extractUserId_WithValidToken_ShouldReturnUserId() {
        // Arrange
        String token = jwtService.generateToken("test@example.com", 1L, "John Doe");

        // Act
        Long userId = jwtService.extractUserId(token);

        // Assert
        assertEquals(1L, userId);
    }

    @Test
    void extractFullName_WithValidToken_ShouldReturnFullName() {
        // Arrange
        String token = jwtService.generateToken("test@example.com", 1L, "John Doe");

        // Act
        String fullName = jwtService.extractFullName(token);

        // Assert
        assertEquals("John Doe", fullName);
    }

    @Test
    void isTokenValid_WithValidTokenAndUser_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithValidTokenOnly_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(isValid);
    }

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

    @Test
    void isTokenValid_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractClaim_WithValidToken_ShouldReturnClaim() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, claims -> claims.getSubject());

        // Assert
        assertEquals("test@example.com", subject);
    }

    @Test
    void extractClaim_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.extractClaim(invalidToken, claims -> claims.getSubject());
        });
    }

    @Test
    void getExpirationTime_ShouldReturnConfiguredValue() {
        // Act
        long expirationTime = jwtService.getExpirationTime();

        // Assert
        assertEquals(86400000L, expirationTime);
    }

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