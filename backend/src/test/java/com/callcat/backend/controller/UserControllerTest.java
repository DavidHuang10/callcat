package com.callcat.backend.controller;

import com.callcat.backend.config.TestSecurityConfig;
import com.callcat.backend.dto.*;
import com.callcat.backend.service.UserService;
import com.callcat.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private UserService userService;
    
    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private com.callcat.backend.service.TokenBlacklistService tokenBlacklistService;

    private UserResponse testUserResponse;
    private UserPreferencesResponse testPreferencesResponse;
    
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponse(1L, "test@example.com", "John", "Doe");
        testPreferencesResponse = new UserPreferencesResponse(
                "UTC", true, "voice123", "Be helpful and concise"
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserProfile_WithAuthentication_ShouldReturnUserResponse() throws Exception {
        when(userService.getUserProfile("test@example.com")).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getUserProfile_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_WithValidData_ShouldReturnUpdatedProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Smith", "jane@example.com");
        UserResponse updatedResponse = new UserResponse(1L, "jane@example.com", "Jane", "Smith");
        
        when(userService.updateProfile(eq("test@example.com"), any(UpdateProfileRequest.class)))
                .thenReturn(updatedResponse);
        
        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_WithValidData_ShouldReturnSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword123", "newPassword123");
        
        doNothing().when(userService).changePassword(eq("test@example.com"), anyString(), anyString());
        
        mockMvc.perform(post("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    void getUserPreferences_ShouldReturnPreferences() throws Exception {
        when(userService.getUserPreferences("test@example.com")).thenReturn(testPreferencesResponse);
        
        mockMvc.perform(get("/api/user/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("UTC"))
                .andExpect(jsonPath("$.emailNotifications").value(true))
                .andExpect(jsonPath("$.voiceId").value("voice123"))
                .andExpect(jsonPath("$.systemPrompt").value("Be helpful and concise"));
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    void updateUserPreferences_WithValidData_ShouldReturnUpdatedPreferences() throws Exception {
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                "America/New_York", false, "voice456", "Be more formal"
        );
        UserPreferencesResponse updatedResponse = new UserPreferencesResponse(
                "America/New_York", false, "voice456", "Be more formal"
        );
        
        when(userService.updateUserPreferences(eq("test@example.com"), any(UpdatePreferencesRequest.class)))
                .thenReturn(updatedResponse);
        
        mockMvc.perform(put("/api/user/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.emailNotifications").value(false))
                .andExpect(jsonPath("$.voiceId").value("voice456"))
                .andExpect(jsonPath("$.systemPrompt").value("Be more formal"));
    }
}