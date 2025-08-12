package com.callcat.backend.controller;

import com.callcat.backend.TestBackendApplication;
import com.callcat.backend.config.TestSecurityConfig;
import com.callcat.backend.dto.AuthResponse;
import com.callcat.backend.entity.User;
import com.callcat.backend.service.AuthenticationService;
import com.callcat.backend.service.JwtService;
import com.callcat.backend.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for AuthController REST endpoints
// Tests the web layer (controllers) in isolation using MockMvc
// Mocks all service dependencies to focus solely on HTTP request/response handling
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private com.callcat.backend.service.TokenBlacklistService tokenBlacklistService;
    
    @MockitoBean
    private VerificationService verificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setIsActive(true);
    }

    // Tests successful user registration with valid input data
    // Verifies that the endpoint returns proper JWT token and user information
    @Test
    void register_WithValidData_ShouldReturnAuthResponse() throws Exception {
        // Arrange
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "test@example.com");
        registerRequest.put("password", "StrongPass123");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");

        String token = "jwt-token";
        AuthResponse authResponse = new AuthResponse(token, 1L, "test@example.com", "John Doe", 86400000L);
        when(authenticationService.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L));
    }

    // Tests input validation for registration endpoint
    // Verifies that invalid email formats and weak passwords are rejected
    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "invalid-email");
        registerRequest.put("password", "weak");
        registerRequest.put("firstName", "");
        registerRequest.put("lastName", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    // Tests duplicate email prevention during registration
    // Ensures that users cannot register with an email that already exists
    @Test
    void register_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "existing@example.com");
        registerRequest.put("password", "StrongPass123");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");

        when(authenticationService.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // Tests successful user authentication with correct credentials
    // Verifies that valid email/password returns JWT token and user data
    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password");

        String token = "jwt-token";
        AuthResponse authResponse = new AuthResponse(token, 1L, "test@example.com", "John Doe", 86400000L);
        when(authenticationService.authenticate(anyString(), anyString())).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    // Tests authentication failure with incorrect credentials
    // Ensures that wrong passwords are rejected with proper error message
    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "wrongpassword");

        when(authenticationService.authenticate(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }


}