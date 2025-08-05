package com.callcat.backend.controller;

import com.callcat.backend.config.TestSecurityConfig;
import com.callcat.backend.entity.User;
import com.callcat.backend.service.AuthenticationService;
import com.callcat.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for UserController REST endpoints
// Tests the web layer (controllers) in isolation using MockMvc
// Focuses on user-specific operations like profile retrieval
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;
    
    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private com.callcat.backend.service.TokenBlacklistService tokenBlacklistService;

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

    // Tests protected endpoint access with valid authentication
    // Verifies that authenticated users can retrieve their profile information
    @Test
    @WithMockUser(username = "test@example.com")
    void getCurrentUser_WithAuthentication_ShouldReturnUserResponse() throws Exception {
        // Arrange
        when(authenticationService.getCurrentUser("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    // Tests security protection for authenticated endpoints
    // Ensures that unauthenticated requests to protected resources are blocked
    @Test
    void getCurrentUser_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isForbidden());
    }
}