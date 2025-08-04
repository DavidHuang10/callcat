package com.callcat.backend.controller;

import com.callcat.backend.entity.User;
import com.callcat.backend.service.AuthenticationService;
import com.callcat.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

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

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() throws Exception {
        // Arrange
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "test@example.com");
        registerRequest.put("password", "StrongPass123");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");

        String token = "jwt-token";
        when(authenticationService.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(token);
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");
        when(jwtService.extractUserId(token)).thenReturn(1L);
        when(jwtService.extractFullName(token)).thenReturn("John Doe");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.expirationTime").value(86400000L));
    }

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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        // Arrange
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password");

        String token = "jwt-token";
        when(authenticationService.authenticate(anyString(), anyString())).thenReturn(token);
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");
        when(jwtService.extractUserId(token)).thenReturn(1L);
        when(jwtService.extractFullName(token)).thenReturn("John Doe");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCurrentUser_WithAuthentication_ShouldReturnUserResponse() throws Exception {
        // Arrange
        when(authenticationService.getCurrentUser("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getCurrentUser_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatePassword_WithStrongPassword_ShouldReturnValid() throws Exception {
        // Arrange
        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("password", "StrongPass123");

        when(authenticationService.validatePasswordStrength("StrongPass123")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/validate-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Password is strong"));
    }

    @Test
    void validatePassword_WithWeakPassword_ShouldReturnInvalid() throws Exception {
        // Arrange
        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("password", "weak");

        when(authenticationService.validatePasswordStrength("weak")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/validate-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number"));
    }
}