package com.callcat.backend.service;

import com.callcat.backend.dto.AuthResponse;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Unit tests for AuthenticationService business logic
// Tests core authentication functionality in isolation using Mockito mocks
// Focuses on service layer logic without database or web layer dependencies
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepositoryDynamoDb userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserDynamoDb testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserDynamoDb();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIsActive(true);
    }

    // Tests user registration business logic with valid input
    // Verifies complete registration flow: email check, password encoding, user creation, JWT generation
    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        // Arrange
        String email = "newuser@example.com";
        String password = "StrongPass123";
        String firstName = "Jane";
        String lastName = "Smith";
        String expectedToken = "jwt-token";
        long expectedExpiration = 86400000L;

        // Create the saved user after registration completion
        UserDynamoDb savedUser = new UserDynamoDb();
        savedUser.setEmail(email);
        savedUser.setFirstName(firstName);
        savedUser.setLastName(lastName);
        savedUser.setIsActive(true);

        when(verificationService.isEmailVerified(email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        // save is void
        // Use any() for the second argument (userId) which is Long but can be null
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn(expectedToken);
        when(jwtService.getExpirationTime()).thenReturn(expectedExpiration);

        // Act
        AuthResponse result = authenticationService.register(email, password, firstName, lastName);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertNull(result.getUserId()); // UserId is null in DynamoDB (userId field in AuthResponse is Long)
        assertEquals(email, result.getEmail());
        assertEquals(firstName + " " + lastName, result.getFullName());
        assertEquals(expectedExpiration, result.getExpiresIn());
        verify(verificationService).isEmailVerified(email);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(UserDynamoDb.class));
        verify(jwtService).generateToken(eq(email), any(), eq(firstName + " " + lastName));
    }

    // Tests that registration requires email verification first
    // Ensures that unverified emails are rejected during registration
    @Test
    void register_WithUnverifiedEmail_ShouldThrowException() {
        // Arrange
        String email = "unverified@example.com";
        when(verificationService.isEmailVerified(email)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.register(email, "Password123", "John", "Doe"));

        assertEquals("Email must be verified before registration. Please verify your email first.", exception.getMessage());
        verify(verificationService).isEmailVerified(email);
    }
    
    // Tests business rule enforcement for already registered emails
    // Ensures that duplicate account registration attempts are properly rejected
    @Test
    void register_WithAlreadyRegisteredEmail_ShouldThrowException() {
        // Arrange
        String email = "existing@example.com";
        UserDynamoDb existingUser = new UserDynamoDb();
        existingUser.setEmail(email);
        
        when(verificationService.isEmailVerified(email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.register(email, "Password123", "John", "Doe"));

        assertEquals("Email already registered", exception.getMessage());
        verify(verificationService).isEmailVerified(email);
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests password strength validation in the business layer
    // Verifies that weak passwords are rejected after email verification check
    @Test
    void register_WithWeakPassword_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String weakPassword = "weak";
        
        when(verificationService.isEmailVerified(email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.register(email, weakPassword, "John", "Doe"));

        assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
        verify(userRepository, never()).save(any(UserDynamoDb.class));
    }

    // Tests authentication business logic with correct credentials
    // Verifies complete login flow: credential validation, user lookup, JWT generation
    @Test
    void authenticate_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String expectedToken = "jwt-token";
        long expectedExpiration = 86400000L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(anyString(), any(), anyString())).thenReturn(expectedToken);
        when(jwtService.getExpirationTime()).thenReturn(expectedExpiration);

        // Act
        AuthResponse result = authenticationService.authenticate(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertNull(result.getUserId()); // UserId is null in DynamoDB
        assertEquals(email, result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        assertEquals(expectedExpiration, result.getExpiresIn());
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(email, password));
        verify(userRepository).findByEmail(email);
    }

    // Tests authentication failure handling for wrong credentials
    // Ensures that bad credentials are properly detected and rejected
    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.authenticate(email, password));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    // Tests account status validation during authentication
    // Ensures that inactive or deleted user accounts cannot authenticate
    @Test
    void authenticate_WithInactiveUser_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        UserDynamoDb inactiveUser = new UserDynamoDb();
        inactiveUser.setIsActive(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.authenticate(email, password));

        assertEquals("User is inactive", exception.getMessage());
    }

    // Tests user profile retrieval with valid email address
    // Verifies that active users can be found and returned by email
    @Test
    void getCurrentUser_WithValidEmail_ShouldReturnUser() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        com.callcat.backend.entity.User result = authenticationService.getCurrentUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertEquals(testUser.getIsActive(), result.getIsActive());
        verify(userRepository).findByEmail(email);
    }

    // Tests error handling for non-existent user lookup
    // Ensures that requests for missing users are properly rejected
    @Test
    void getCurrentUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.getCurrentUser(email));

        assertEquals("User not found", exception.getMessage());
    }

}