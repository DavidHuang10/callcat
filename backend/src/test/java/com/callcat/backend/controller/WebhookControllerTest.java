package com.callcat.backend.controller;

import com.callcat.backend.config.TestSecurityConfig;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.TranscriptService;
import com.callcat.backend.entity.CallRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for WebhookController REST endpoints
// Tests the web layer (controllers) in isolation using MockMvc
// Mocks all service dependencies to focus solely on HTTP request/response handling
@WebMvcTest(WebhookController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CallService callService;

    @MockitoBean
    private TranscriptService transcriptService;

    // Mock security-related services that are autowired in the application context
    @MockitoBean
    private com.callcat.backend.service.JwtService jwtService;
    
    @MockitoBean
    private com.callcat.backend.service.TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private CallRecord mockCallRecord;

    @BeforeEach
    void setUp() {
        mockCallRecord = new CallRecord();
        mockCallRecord.setCallId("test-call-id");
        mockCallRecord.setUserId("test-user-id");
        mockCallRecord.setProviderId("retell-call-123");
        mockCallRecord.setStatus("SCHEDULED");
        mockCallRecord.setPhoneNumber("+1234567890");
    }

    @Test
    void testHandleRetellWebhook_CallStarted() throws Exception {
        // Given
        String payload = """
            {
                "event": "call_started",
                "call": {
                    "call_id": "retell-call-123",
                    "from_number": "+1234567890",
                    "to_number": "+0987654321"
                }
            }
            """;

        when(callService.findCallByProviderId("retell-call-123")).thenReturn(mockCallRecord);

        // When & Then
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNoContent());

        // Verify call service interactions
        verify(callService).findCallByProviderId("retell-call-123");
        verify(callService).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testHandleRetellWebhook_CallEnded() throws Exception {
        // Given
        String payload = """
            {
                "event": "call_ended",
                "call": {
                    "call_id": "retell-call-123",
                    "end_timestamp": 1693123456789
                }
            }
            """;

        when(callService.findCallByProviderId("retell-call-123")).thenReturn(mockCallRecord);

        // When & Then
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNoContent());

        // Verify call service interactions
        verify(callService).findCallByProviderId("retell-call-123");
        verify(callService).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testHandleRetellWebhook_CallAnalyzed() throws Exception {
        // Given
        String payload = """
            {
                "event": "call_analyzed",
                "call": {
                    "call_id": "retell-call-123",
                    "analysis": {
                        "summary": "Call completed successfully"
                    }
                }
            }
            """;

        when(callService.findCallByProviderId("retell-call-123")).thenReturn(mockCallRecord);

        // When & Then
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNoContent());

        // Verify call service interactions
        verify(callService).findCallByProviderId("retell-call-123");
        verify(callService).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testHandleRetellWebhook_UnknownEvent() throws Exception {
        // Given
        String payload = """
            {
                "event": "unknown_event",
                "call": {
                    "call_id": "retell-call-123"
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNoContent());

        // Verify no service calls were made for unknown events
        verify(callService, never()).findCallByProviderId(anyString());
        verify(callService, never()).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testHandleRetellWebhook_ExceptionHandling() throws Exception {
        // Given
        String payload = """
            {
                "event": "call_started",
                "call": {
                    "call_id": "retell-call-123"
                }
            }
            """;

        when(callService.findCallByProviderId("retell-call-123"))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then - Should still return 204 to avoid retries
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isNoContent());

        // Verify the service was called but exception was handled
        verify(callService).findCallByProviderId("retell-call-123");
        verify(callService, never()).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testHandleRetellWebhook_MalformedPayload() throws Exception {
        // Given
        String malformedPayload = "{ invalid json }";

        // When & Then - Should return 400 for malformed JSON
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedPayload))
                .andExpect(status().isBadRequest());

        // Verify no service calls were made
        verify(callService, never()).findCallByProviderId(anyString());
        verify(callService, never()).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testHandleRetellWebhook_NullPayload() throws Exception {
        // When & Then - Should return 400 for empty request body
        mockMvc.perform(post("/webhooks/retell")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify no service calls were made
        verify(callService, never()).findCallByProviderId(anyString());
        verify(callService, never()).saveCallRecord(any(CallRecord.class));
    }
}