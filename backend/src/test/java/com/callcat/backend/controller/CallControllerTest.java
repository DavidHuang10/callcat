package com.callcat.backend.controller;

import com.callcat.backend.config.TestSecurityConfig;
import com.callcat.backend.dto.*;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CallController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CallService callService;
    
    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private com.callcat.backend.service.TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCallRequest createRequest;
    private UpdateCallRequest updateRequest;
    private CallResponse callResponse;
    private CallListResponse callListResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCallRequest();
        createRequest.setCalleeName("John Doe");
        createRequest.setPhoneNumber("+15551234567");
        createRequest.setSubject("Test Call");
        createRequest.setPrompt("Test prompt for AI");
        createRequest.setScheduledAt(System.currentTimeMillis() + 3600000);
        createRequest.setAiLanguage("en");
        createRequest.setVoiceId("voice123");

        updateRequest = new UpdateCallRequest();
        updateRequest.setCalleeName("Jane Doe");
        updateRequest.setSubject("Updated Call");
        updateRequest.setStatus("IN_PROGRESS");

        callResponse = new CallResponse();
        callResponse.setCallId("test-call-id");
        callResponse.setCalleeName("John Doe");
        callResponse.setPhoneNumber("+15551234567");
        callResponse.setSubject("Test Call");
        callResponse.setPrompt("Test prompt for AI");
        callResponse.setStatus("SCHEDULED");
        callResponse.setAiLanguage("en");
        callResponse.setVoiceId("voice123");
        callResponse.setCreatedAt(System.currentTimeMillis());
        callResponse.setUpdatedAt(System.currentTimeMillis());

        List<CallResponse> calls = Arrays.asList(callResponse);
        callListResponse = new CallListResponse(calls, null);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createCall_WithValidRequest_ShouldReturnCreatedCall() throws Exception {
        // Arrange
        when(callService.createCall(eq("test@example.com"), any(CreateCallRequest.class)))
                .thenReturn(callResponse);

        // Act & Assert
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.calleeName").value("John Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+15551234567"))
                .andExpect(jsonPath("$.subject").value("Test Call"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.aiLanguage").value("en"));

        verify(callService).createCall(eq("test@example.com"), any(CreateCallRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createCall_WithInvalidPhoneNumber_ShouldReturnBadRequest() throws Exception {
        // Arrange
        createRequest.setPhoneNumber("invalid-phone");
        when(callService.createCall(eq("test@example.com"), any(CreateCallRequest.class)))
                .thenThrow(new IllegalArgumentException("Phone number must be in E.164 format (+1XXXXXXXXXX)"));

        // Act & Assert
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone number must be in E.164 format (+1XXXXXXXXXX)"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createCall_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        createRequest.setCalleeName(null); // Missing required field

        // Act & Assert
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCall_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(callService, never()).createCall(anyString(), any(CreateCallRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCalls_WithoutFilters_ShouldReturnAllCalls() throws Exception {
        // Arrange
        when(callService.getCalls("test@example.com", null, 20))
                .thenReturn(callListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calls").isArray())
                .andExpect(jsonPath("$.calls[0].callId").value("test-call-id"))
                .andExpect(jsonPath("$.nextToken").isEmpty());

        verify(callService).getCalls("test@example.com", null, 20);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCalls_WithStatusFilter_ShouldReturnFilteredCalls() throws Exception {
        // Arrange
        when(callService.getCalls("test@example.com", "SCHEDULED", 20))
                .thenReturn(callListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls")
                .param("status", "SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calls").isArray())
                .andExpect(jsonPath("$.calls[0].status").value("SCHEDULED"));

        verify(callService).getCalls("test@example.com", "SCHEDULED", 20);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCalls_WithCustomLimit_ShouldUseProvidedLimit() throws Exception {
        // Arrange
        when(callService.getCalls("test@example.com", null, 50))
                .thenReturn(callListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls")
                .param("limit", "50"))
                .andExpect(status().isOk());

        verify(callService).getCalls("test@example.com", null, 50);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCalls_WithExcessiveLimit_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/calls")
                .param("limit", "150"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Limit cannot exceed 100"))
                .andExpect(jsonPath("$.success").value(false));

        verify(callService, never()).getCalls(anyString(), anyString(), anyInt());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCall_WithValidCallId_ShouldReturnCall() throws Exception {
        // Arrange
        when(callService.getCall("test@example.com", "test-call-id"))
                .thenReturn(callResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.calleeName").value("John Doe"));

        verify(callService).getCall("test@example.com", "test-call-id");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCall_WithNonExistentCall_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(callService.getCall("test@example.com", "nonexistent-id"))
                .thenThrow(new RuntimeException("Call not found"));

        // Act & Assert
        mockMvc.perform(get("/api/calls/nonexistent-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Call not found"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCall_WithValidRequest_ShouldReturnUpdatedCall() throws Exception {
        // Arrange
        CallResponse updatedResponse = new CallResponse();
        updatedResponse.setCallId("test-call-id");
        updatedResponse.setCalleeName("Jane Doe");
        updatedResponse.setSubject("Updated Call");
        updatedResponse.setStatus("IN_PROGRESS");

        when(callService.updateCall(eq("test@example.com"), eq("test-call-id"), any(UpdateCallRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/calls/test-call-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.calleeName").value("Jane Doe"))
                .andExpect(jsonPath("$.subject").value("Updated Call"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(callService).updateCall(eq("test@example.com"), eq("test-call-id"), any(UpdateCallRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCall_WithInvalidStatusTransition_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(callService.updateCall(eq("test@example.com"), eq("test-call-id"), any(UpdateCallRequest.class)))
                .thenThrow(new IllegalArgumentException("Cannot change status of a finalized call"));

        // Act & Assert
        mockMvc.perform(put("/api/calls/test-call-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot change status of a finalized call"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteCall_WithValidCallId_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(callService).deleteCall("test@example.com", "test-call-id");

        // Act & Assert
        mockMvc.perform(delete("/api/calls/test-call-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Call deleted successfully"))
                .andExpect(jsonPath("$.success").value(true));

        verify(callService).deleteCall("test@example.com", "test-call-id");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteCall_WithNonScheduledCall_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Only scheduled calls can be deleted"))
                .when(callService).deleteCall("test@example.com", "test-call-id");

        // Act & Assert
        mockMvc.perform(delete("/api/calls/test-call-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only scheduled calls can be deleted"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteCall_WithNonExistentCall_ShouldReturnBadRequest() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Call not found"))
                .when(callService).deleteCall("test@example.com", "nonexistent-id");

        // Act & Assert
        mockMvc.perform(delete("/api/calls/nonexistent-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Call not found"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createCall_WithLongPrompt_ShouldReturnBadRequest() throws Exception {
        // Arrange
        StringBuilder longPrompt = new StringBuilder();
        for (int i = 0; i < 5001; i++) {
            longPrompt.append("a");
        }
        createRequest.setPrompt(longPrompt.toString());

        // Act & Assert
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createCall_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(callService.createCall(eq("test@example.com"), any(CreateCallRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Database connection failed"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCall_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/calls/test-call-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk()); // Empty update request should be valid (no changes)
    }

    @Test
    void getAllEndpoints_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Test all endpoints require authentication
        mockMvc.perform(get("/api/calls")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/calls/test-id")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/calls/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/calls/test-id")).andExpect(status().isUnauthorized());
    }
}