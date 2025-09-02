package com.callcat.backend.service;

import com.callcat.backend.dto.CallResponse;
import com.callcat.backend.dto.UserPreferencesResponse;
import com.callcat.backend.entity.CallRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetellServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CallService callService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private RetellService retellService;

    private CallRecord mockCallRecord;
    private UserPreferencesResponse mockUserPreferences;

    @BeforeEach
    void setUp() {
        // Set up test configuration values
        ReflectionTestUtils.setField(retellService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(retellService, "baseUrl", "https://api.retell.ai");
        ReflectionTestUtils.setField(retellService, "phoneNumber", "+1234567890");
        ReflectionTestUtils.setField(retellService, "restClient", restClient);

        // Set up mock objects
        mockCallRecord = new CallRecord();
        mockCallRecord.setCallId("test-call-id");
        mockCallRecord.setUserId("2");
        mockCallRecord.setPhoneNumber("+0987654321");
        mockCallRecord.setPrompt("Test call prompt");
        mockCallRecord.setStatus("SCHEDULED");

        mockUserPreferences = new UserPreferencesResponse();
        mockUserPreferences.setSystemPrompt("Test system prompt");
    }

    @Test
    void testMakeCall_Success() throws Exception {
        // Given
        String callId = "test-call-id";
        String retellResponseJson = """
            {
                "call_id": "retell-call-123",
                "from_number": "+1234567890",
                "to_number": "+0987654321",
                "status": "registered"
            }
            """;

        when(callService.findCallByCallId(callId)).thenReturn(mockCallRecord);
        when(userService.getUserPreferences(2L)).thenReturn(mockUserPreferences);

        // Mock RestClient chain
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/create-phone-call")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(retellResponseJson);

        // When
        CallResponse result = retellService.makeCall(callId);

        // Then
        assertNotNull(result);
        assertEquals("test-call-id", result.getCallId());
        assertEquals("retell-call-123", result.getProviderId());
        assertEquals("+1234567890", result.getCallerNumber());

        // Verify service interactions
        verify(callService).findCallByCallId(callId);
        verify(userService).getUserPreferences(2L);
        verify(callService).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testMakeCall_RestClientException() {
        // Given
        String callId = "test-call-id";

        when(callService.findCallByCallId(callId)).thenReturn(mockCallRecord);
        when(userService.getUserPreferences(2L)).thenReturn(mockUserPreferences);

        // Mock RestClient to throw exception
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/create-phone-call")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenThrow(new RuntimeException("API call failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            retellService.makeCall(callId);
        });

        assertEquals("Failed to create call", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("API call failed"));

        // Verify services were called but call was not saved
        verify(callService).findCallByCallId(callId);
        verify(userService).getUserPreferences(2L);
        verify(callService, never()).saveCallRecord(any(CallRecord.class));
    }

    @Test
    void testMakeCall_CallServiceException() {
        // Given
        String callId = "test-call-id";

        when(callService.findCallByCallId(callId)).thenThrow(new RuntimeException("Call not found"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            retellService.makeCall(callId);
        });

        assertEquals("Failed to create call", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Call not found"));

        // Verify only findCallByCallId was called
        verify(callService).findCallByCallId(callId);
        verify(userService, never()).getUserPreferences(anyString());
        verify(callService, never()).saveCallRecord(any(CallRecord.class));
    }
}