package com.callcat.backend.service;

import com.callcat.backend.entity.User;
import com.callcat.backend.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIsActive(true);
    }

    @Test
    void register_WithValidData_ShouldReturnJwtToken() {
        // Arrange
        String email = "newuser@example.com";
        String password = "StrongPass123";
        String firstName = "Jane";
        String lastName = "Smith";
        String expectedToken = "jwt-token";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(anyString(), any(Long.class), anyString())).thenReturn(expectedToken);

        // Act
        String result = authenticationService.register(email, password, firstName, lastName);

        // Assert
        assertEquals(expectedToken, result);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(email, testUser.getId(), firstName + " " + lastName);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.register(email, "password", "John", "Doe"));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithWeakPassword_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String weakPassword = "weak";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.register(email, weakPassword, "John", "Doe"));

        assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnJwtToken() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String expectedToken = "jwt-token";

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(anyString(), any(Long.class), anyString())).thenReturn(expectedToken);

        // Act
        String result = authenticationService.authenticate(email, password);

        // Assert
        assertEquals(expectedToken, result);
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(email, password));
        verify(userRepository).findByEmailAndIsActive(email, true);
    }

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

    @Test
    void authenticate_WithInactiveUser_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.authenticate(email, password));

        assertEquals("User not found or inactive", exception.getMessage());
    }

    @Test
    void getCurrentUser_WithValidEmail_ShouldReturnUser() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.of(testUser));

        // Act
        User result = authenticationService.getCurrentUser(email);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByEmailAndIsActive(email, true);
    }

    @Test
    void getCurrentUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmailAndIsActive(email, true)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.getCurrentUser(email));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void validatePasswordStrength_WithStrongPassword_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(authenticationService.validatePasswordStrength("StrongPass123"));
    }

    @Test
    void validatePasswordStrength_WithWeakPassword_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(authenticationService.validatePasswordStrength("weak"));
        assertFalse(authenticationService.validatePasswordStrength("nouppercase123"));
        assertFalse(authenticationService.validatePasswordStrength("NOLOWERCASE123"));
        assertFalse(authenticationService.validatePasswordStrength("NoNumbers"));
    }
}