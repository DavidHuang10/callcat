package com.callcat.backend.controller;

import com.callcat.backend.config.TestSecurityConfig;
import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.service.TranscriptService;
import com.callcat.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranscriptController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TranscriptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TranscriptService transcriptService;
    
    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private com.callcat.backend.service.TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private TranscriptResponse transcriptResponse;

    @BeforeEach
    void setUp() {
        transcriptResponse = new TranscriptResponse();
        transcriptResponse.setCallId("test-call-id");
        transcriptResponse.setTranscriptText("Agent: Hello! How can I help you today?\nUser: I'd like to schedule an appointment.\nAgent: I'd be happy to help you with that.");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithExistingTranscript_ShouldReturnTranscript() throws Exception {
        // Arrange
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenReturn(transcriptResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.transcriptText").value("Agent: Hello! How can I help you today?\nUser: I'd like to schedule an appointment.\nAgent: I'd be happy to help you with that."));

        verify(transcriptService).getTranscript("test@example.com", "test-call-id");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithEmptyTranscript_ShouldReturnEmptyTranscript() throws Exception {
        // Arrange
        TranscriptResponse emptyResponse = new TranscriptResponse("test-call-id", "");
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.transcriptText").value(""));

        verify(transcriptService).getTranscript("test@example.com", "test-call-id");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithNonExistentCall_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(transcriptService.getTranscript("test@example.com", "nonexistent-call"))
                .thenThrow(new RuntimeException("Call not found"));

        // Act & Assert
        mockMvc.perform(get("/api/calls/nonexistent-call/transcript"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Call not found"))
                .andExpect(jsonPath("$.success").value(false));

        verify(transcriptService).getTranscript("test@example.com", "nonexistent-call");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithCallNotStarted_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(transcriptService.getTranscript("test@example.com", "scheduled-call"))
                .thenThrow(new RuntimeException("Call has not started yet - no transcript available"));

        // Act & Assert
        mockMvc.perform(get("/api/calls/scheduled-call/transcript"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Call has not started yet - no transcript available"))
                .andExpect(jsonPath("$.success").value(false));

        verify(transcriptService).getTranscript("test@example.com", "scheduled-call");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithLongTranscript_ShouldReturnCompleteTranscript() throws Exception {
        // Arrange
        StringBuilder longTranscript = new StringBuilder();
        longTranscript.append("Agent: Welcome to our service! How can I assist you today?\n");
        longTranscript.append("User: Hi, I'm calling about my account.\n");
        longTranscript.append("Agent: I'd be happy to help you with your account. Can you please provide your account number?\n");
        longTranscript.append("User: Sure, it's 123456789.\n");
        longTranscript.append("Agent: Thank you. I can see your account here. What specific question do you have?\n");
        longTranscript.append("User: I want to update my billing information.\n");
        longTranscript.append("Agent: I can help you with that. For security purposes, can you please verify your date of birth?\n");
        longTranscript.append("User: It's January 15th, 1985.\n");
        longTranscript.append("Agent: Perfect. Now, what changes would you like to make to your billing information?\n");
        longTranscript.append("User: I need to update my credit card number.\n");
        String longTranscriptText = longTranscript.toString();

        TranscriptResponse longResponse = new TranscriptResponse("test-call-id", longTranscriptText);
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenReturn(longResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.transcriptText").value(longTranscriptText));

        verify(transcriptService).getTranscript("test@example.com", "test-call-id");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithSpecialCharacters_ShouldReturnCorrectTranscript() throws Exception {
        // Arrange
        String specialCharTranscript = "Agent: Hello! How can I help you today? 😊\n" +
                "User: I'd like to schedule an appointment for tomorrow @ 3:00 PM.\n" +
                "Agent: Great! I can help with that. The cost will be $50.00.\n" +
                "User: That's perfect! My email is user@example.com & phone is (555) 123-4567.";
        
        TranscriptResponse specialResponse = new TranscriptResponse("test-call-id", specialCharTranscript);
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenReturn(specialResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.transcriptText").value(specialCharTranscript));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithUserNotFound_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getTranscript_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isUnauthorized());

        verify(transcriptService, never()).getTranscript(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithDifferentCallIds_ShouldCallServiceWithCorrectId() throws Exception {
        // Arrange
        String[] callIds = {"call-1", "call-2", "call-3", "very-long-call-id-12345"};
        
        for (String callId : callIds) {
            TranscriptResponse response = new TranscriptResponse(callId, "Sample transcript for " + callId);
            when(transcriptService.getTranscript("test@example.com", callId))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/calls/" + callId + "/transcript"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.callId").value(callId))
                    .andExpect(jsonPath("$.transcriptText").value("Sample transcript for " + callId));

            verify(transcriptService).getTranscript("test@example.com", callId);
        }
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Database connection failed"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "user1@example.com")
    void getTranscript_WithDifferentUser_ShouldCallServiceWithCorrectUser() throws Exception {
        // Arrange
        TranscriptResponse response = new TranscriptResponse("test-call-id", "User1's transcript");
        when(transcriptService.getTranscript("user1@example.com", "test-call-id"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcriptText").value("User1's transcript"));

        verify(transcriptService).getTranscript("user1@example.com", "test-call-id");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_WithInProgressCall_ShouldReturnPartialTranscript() throws Exception {
        // Arrange
        String partialTranscript = "Agent: Hello! How can I help you today?\nUser: I'd like to schedule an appointment.\nAgent: I'd be happy to help...";
        TranscriptResponse response = new TranscriptResponse("test-call-id", partialTranscript);
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callId").value("test-call-id"))
                .andExpect(jsonPath("$.transcriptText").value(partialTranscript));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTranscript_ResponseStructure_ShouldMatchExpectedFormat() throws Exception {
        // Arrange
        when(transcriptService.getTranscript("test@example.com", "test-call-id"))
                .thenReturn(transcriptResponse);

        // Act & Assert
        mockMvc.perform(get("/api/calls/test-call-id/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.callId").exists())
                .andExpect(jsonPath("$.transcriptText").exists())
                .andExpect(jsonPath("$.callId").isString())
                .andExpect(jsonPath("$.transcriptText").isString());
    }
}