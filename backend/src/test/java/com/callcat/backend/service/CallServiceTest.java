package com.callcat.backend.service;

import com.callcat.backend.dto.*;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallServiceTest {

    @Mock
    private CallRecordRepository callRecordRepository;

    @Mock
    private UserRepositoryDynamoDb userRepository;

    @Mock
    private EventBridgeService eventBridgeService;

    @InjectMocks
    private CallService callService;

    private UserDynamoDb testUser;
    private CallRecord testCall;
    private CallRequest createRequest;
    private UpdateCallRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserDynamoDb();
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);

        testCall = new CallRecord();
        testCall.setUserId("test@example.com");
        testCall.setCallId("test-call-id");
        testCall.setCalleeName("John Doe");
        testCall.setPhoneNumber("+15551234567");
        testCall.setSubject("Test Call");
        testCall.setPrompt("Test prompt for AI");
        testCall.setStatus("SCHEDULED");
        testCall.setScheduledFor(System.currentTimeMillis() + 3600000); // 1 hour from now
        testCall.setAiLanguage("en");
        testCall.setVoiceId("voice123");
        testCall.setCreatedAt(System.currentTimeMillis());
        testCall.setUpdatedAt(System.currentTimeMillis());

        createRequest = new CallRequest();
        createRequest.setCalleeName("John Doe");
        createRequest.setPhoneNumber("+15551234567");
        createRequest.setSubject("Test Call");
        createRequest.setPrompt("Test prompt for AI");
        createRequest.setScheduledFor(System.currentTimeMillis() + 3600000);
        createRequest.setAiLanguage("en");
        createRequest.setVoiceId("voice123");

        updateRequest = new UpdateCallRequest();
        updateRequest.setCalleeName("Jane Doe");
        updateRequest.setSubject("Updated Call");
    }

    @Test
    void createCall_WithValidRequest_ShouldReturnCallResponse() {
        // Arrange
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        CallResponse result = callService.createCall("test@example.com", createRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCallId());
        assertEquals("John Doe", result.getCalleeName());
        assertEquals("+15551234567", result.getPhoneNumber());
        assertEquals("Test Call", result.getSubject());
        assertEquals("SCHEDULED", result.getStatus());
        assertEquals("en", result.getAiLanguage());

        verify(userRepository).findByEmail("test@example.com");
        verify(callRecordRepository).save(any(CallRecord.class));
    }

    @Test
    void createCall_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.createCall("nonexistent@example.com", createRequest));

        assertEquals("User not found", exception.getMessage());
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void createCall_WithInvalidPhoneNumber_ShouldThrowException() {
        // Arrange
        createRequest.setPhoneNumber("invalid-phone");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.createCall("test@example.com", createRequest));

        assertEquals("Phone number must be in E.164 format (+1XXXXXXXXXX)", exception.getMessage());
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void createCall_WithPastScheduledTime_ShouldThrowException() {
        // Arrange
        createRequest.setScheduledFor(System.currentTimeMillis() - 3600000); // 1 hour ago
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.createCall("test@example.com", createRequest));

        assertEquals("Scheduled time must be in the future", exception.getMessage());
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void createCall_WithNullScheduledTime_ShouldUseCurrentTime() {
        // Arrange
        createRequest.setScheduledFor(null);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        CallResponse result = callService.createCall("test@example.com", createRequest);

        // Assert
        assertNotNull(result);
        verify(callRecordRepository).save(any(CallRecord.class));
    }

    @Test
    void createCall_WithNullAiLanguage_ShouldDefaultToEnglish() {
        // Arrange
        createRequest.setAiLanguage(null);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        CallResponse result = callService.createCall("test@example.com", createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("en", result.getAiLanguage());
    }

    @Test
    void getCalls_WithoutStatusFilter_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            callService.getCalls("test@example.com", null, 20);
        });
        
        assertEquals("Status parameter is required", exception.getMessage());
    }

    @Test
    void getCalls_WithStatusFilter_ShouldReturnFilteredCalls() {
        // Arrange
        List<CallRecord> scheduledCalls = Arrays.asList(testCall);
        
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndStatus("test@example.com", "SCHEDULED", 20))
                .thenReturn(scheduledCalls);

        // Act
        CallListResponse result = callService.getCalls("test@example.com", "SCHEDULED", 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCalls().size());
        assertEquals("SCHEDULED", result.getCalls().get(0).getStatus());

        verify(callRecordRepository).findByUserIdAndStatus("test@example.com", "SCHEDULED", 20);
    }

    @Test
    void getCall_WithValidCallId_ShouldReturnCall() {
        // Arrange
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act
        CallResponse result = callService.getCall("test-call-id");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCallId());
        assertEquals("John Doe", result.getCalleeName());

        verify(callRecordRepository).findByCallId("test-call-id");
    }

    @Test
    void getCall_WithNonExistentCall_ShouldThrowException() {
        // Arrange
        when(callRecordRepository.findByCallId("nonexistent-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.getCall("nonexistent-id"));

        assertEquals("Call not found with ID: nonexistent-id", exception.getMessage());
    }

    @Test
    void updateCall_WithValidRequest_ShouldUpdateAndReturnCall() {
        // Arrange
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        CallResponse result = callService.updateCall("test-call-id", updateRequest);

        // Assert
        assertNotNull(result);
        verify(callRecordRepository).save(testCall);
        
        // Verify fields were updated
        assertEquals("Jane Doe", testCall.getCalleeName());
        assertEquals("Updated Call", testCall.getSubject());
    }

    @Test
    void updateCall_WithInvalidPhoneNumber_ShouldThrowException() {
        // Arrange
        updateRequest.setPhoneNumber("invalid-phone");
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.updateCall("test-call-id", updateRequest));

        assertEquals("Phone number must be in E.164 format (+1XXXXXXXXXX)", exception.getMessage());
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }


    @Test
    void updateCall_WithPastScheduledTime_ShouldThrowException() {
        // Arrange
        updateRequest.setScheduledFor(System.currentTimeMillis() - 3600000); // 1 hour ago
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.updateCall("test-call-id", updateRequest));

        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    void deleteCall_WithScheduledCall_ShouldDeleteSuccessfully() {
        // Arrange
        testCall.setStatus("SCHEDULED");
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act
        callService.deleteCall("test-call-id");

        // Assert
        verify(callRecordRepository).delete(testCall);
    }

    @Test
    void deleteCall_WithNonScheduledCall_ShouldThrowException() {
        // Arrange
        testCall.setStatus("COMPLETED");
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.deleteCall("test-call-id"));

        assertEquals("Only scheduled calls can be deleted", exception.getMessage());
        verify(callRecordRepository, never()).delete(any(CallRecord.class));
    }

    @Test
    void deleteCall_WithNonExistentCall_ShouldThrowException() {
        // Arrange
        when(callRecordRepository.findByCallId("nonexistent-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.deleteCall("nonexistent-id"));

        assertEquals("Call not found with ID: nonexistent-id", exception.getMessage());
        verify(callRecordRepository, never()).delete(any(CallRecord.class));
    }

    @Test
    void updateCallStatusWithRetellData_WithValidData_ShouldUpdateCall() {
        // Arrange
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        callService.updateCallStatusWithRetellData("test-call-id", "COMPLETED", 1693123456789L, "retell-123", true);

        // Assert
        verify(callRecordRepository).findByCallId("test-call-id");
        verify(callRecordRepository).save(testCall);
        
        assertEquals("COMPLETED", testCall.getStatus());
        assertEquals("retell-123", testCall.getProviderId());
        assertEquals(1693123456789L, testCall.getCompletedAt());
        assertTrue(testCall.getDialSuccessful());
        assertTrue(testCall.getUpdatedAt() > 0);
    }

    @Test
    void updateCallStatusWithRetellData_WithNullOptionalFields_ShouldUpdateOnlyRequired() {
        // Arrange
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        callService.updateCallStatusWithRetellData("test-call-id", "COMPLETED", null, "retell-123", null);

        // Assert
        verify(callRecordRepository).save(testCall);
        
        assertEquals("COMPLETED", testCall.getStatus());
        assertEquals("retell-123", testCall.getProviderId());
        assertNull(testCall.getCompletedAt()); // Should remain null
        assertNull(testCall.getDialSuccessful()); // Should remain original value (null)
    }

    @Test
    void updateRetellCallData_WithValidData_ShouldUpdateCall() throws Exception {
        // Arrange
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode retellData = mapper.readTree("{\"call_id\":\"retell-123\",\"status\":\"completed\"}");

        // Act
        callService.updateRetellCallData("test-call-id", retellData);

        // Assert
        verify(callRecordRepository).findByCallId("test-call-id");
        verify(callRecordRepository).save(testCall);
        
        assertNotNull(testCall.getRetellCallData());
        assertTrue(testCall.getRetellCallData().contains("retell-123"));
        assertTrue(testCall.getUpdatedAt() > 0);
    }

    @Test
    void findCallByCallId_WithValidId_ShouldReturnCall() {
        // Arrange
        when(callRecordRepository.findByCallId("test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act
        CallRecord result = callService.findCallByCallId("test-call-id");

        // Assert
        assertNotNull(result);
        assertEquals("test-call-id", result.getCallId());
        verify(callRecordRepository).findByCallId("test-call-id");
    }

    @Test
    void findCallByCallId_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(callRecordRepository.findByCallId("invalid-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.findCallByCallId("invalid-id"));

        assertEquals("Call not found with ID: invalid-id", exception.getMessage());
    }

    @Test
    void findCallByProviderId_WithValidId_ShouldReturnCall() {
        // Arrange
        testCall.setProviderId("retell-123");
        when(callRecordRepository.findByProviderId("retell-123"))
                .thenReturn(Optional.of(testCall));

        // Act
        CallRecord result = callService.findCallByProviderId("retell-123");

        // Assert
        assertNotNull(result);
        assertEquals("retell-123", result.getProviderId());
        verify(callRecordRepository).findByProviderId("retell-123");
    }

    @Test
    void findCallByProviderId_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(callRecordRepository.findByProviderId("invalid-provider-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.findCallByProviderId("invalid-provider-id"));

        assertEquals("Call not found with provider ID: invalid-provider-id", exception.getMessage());
    }

    @Test
    void saveCallRecord_WithValidRecord_ShouldSaveSuccessfully() {
        // Arrange
        when(callRecordRepository.save(testCall))
                .thenReturn(testCall);

        // Act
        callService.saveCallRecord(testCall);

        // Assert
        verify(callRecordRepository).save(testCall);
    }

}