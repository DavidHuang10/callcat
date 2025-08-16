package com.callcat.backend.service;

import com.callcat.backend.dto.*;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.UserRepository;
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
    private UserRepository userRepository;

    @InjectMocks
    private CallService callService;

    private User testUser;
    private CallRecord testCall;
    private CallRequest createRequest;
    private CallRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);

        testCall = new CallRecord();
        testCall.setUserId(1L);
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

        updateRequest = new CallRequest();
        updateRequest.setCalleeName("Jane Doe");
        updateRequest.setSubject("Updated Call");
    }

    @Test
    void createCall_WithValidRequest_ShouldReturnCallResponse() {
        // Arrange
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        CallResponse result = callService.createCall("test@example.com", createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test-call-id", result.getCallId());
        assertEquals("John Doe", result.getCalleeName());
        assertEquals("+15551234567", result.getPhoneNumber());
        assertEquals("Test Call", result.getSubject());
        assertEquals("SCHEDULED", result.getStatus());
        assertEquals("en", result.getAiLanguage());

        verify(userRepository).findByEmailAndIsActive("test@example.com", true);
        verify(callRecordRepository).save(any(CallRecord.class));
    }

    @Test
    void createCall_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmailAndIsActive("nonexistent@example.com", true))
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
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
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
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
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
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
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
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
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
    void getCalls_WithoutStatusFilter_ShouldReturnAllCalls() {
        // Arrange
        List<CallRecord> upcomingCalls = Arrays.asList(testCall);
        List<CallRecord> completedCalls = Arrays.asList();
        
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findUpcomingCallsByUserId(1L, 10))
                .thenReturn(upcomingCalls);
        when(callRecordRepository.findCompletedCallsByUserId(1L, 10))
                .thenReturn(completedCalls);

        // Act
        CallListResponse result = callService.getCalls("test@example.com", null, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCalls().size());
        assertEquals("test-call-id", result.getCalls().get(0).getCallId());
        assertNull(result.getNextToken());

        verify(callRecordRepository).findUpcomingCallsByUserId(1L, 10);
        verify(callRecordRepository).findCompletedCallsByUserId(1L, 10);
    }

    @Test
    void getCalls_WithStatusFilter_ShouldReturnFilteredCalls() {
        // Arrange
        List<CallRecord> scheduledCalls = Arrays.asList(testCall);
        
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndStatus(1L, "SCHEDULED", 20))
                .thenReturn(scheduledCalls);

        // Act
        CallListResponse result = callService.getCalls("test@example.com", "SCHEDULED", 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCalls().size());
        assertEquals("SCHEDULED", result.getCalls().get(0).getStatus());

        verify(callRecordRepository).findByUserIdAndStatus(1L, "SCHEDULED", 20);
    }

    @Test
    void getCall_WithValidCallId_ShouldReturnCall() {
        // Arrange
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act
        CallResponse result = callService.getCall("test@example.com", "test-call-id");

        // Assert
        assertNotNull(result);
        assertEquals("test-call-id", result.getCallId());
        assertEquals("John Doe", result.getCalleeName());

        verify(callRecordRepository).findByUserIdAndCallId(1L, "test-call-id");
    }

    @Test
    void getCall_WithNonExistentCall_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "nonexistent-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.getCall("test@example.com", "nonexistent-id"));

        assertEquals("Call not found", exception.getMessage());
    }

    @Test
    void updateCall_WithValidRequest_ShouldUpdateAndReturnCall() {
        // Arrange
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "test-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callRecordRepository.save(any(CallRecord.class)))
                .thenReturn(testCall);

        // Act
        CallResponse result = callService.updateCall("test@example.com", "test-call-id", updateRequest);

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
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.updateCall("test@example.com", "test-call-id", updateRequest));

        assertEquals("Phone number must be in E.164 format (+1XXXXXXXXXX)", exception.getMessage());
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }


    @Test
    void updateCall_WithPastScheduledTime_ShouldThrowException() {
        // Arrange
        updateRequest.setScheduledFor(System.currentTimeMillis() - 3600000); // 1 hour ago
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.updateCall("test@example.com", "test-call-id", updateRequest));

        assertEquals("Scheduled time must be in the future", exception.getMessage());
    }

    @Test
    void deleteCall_WithScheduledCall_ShouldDeleteSuccessfully() {
        // Arrange
        testCall.setStatus("SCHEDULED");
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act
        callService.deleteCall("test@example.com", "test-call-id");

        // Assert
        verify(callRecordRepository).delete(testCall);
    }

    @Test
    void deleteCall_WithNonScheduledCall_ShouldThrowException() {
        // Arrange
        testCall.setStatus("IN_PROGRESS");
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "test-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> callService.deleteCall("test@example.com", "test-call-id"));

        assertEquals("Only scheduled calls can be deleted", exception.getMessage());
        verify(callRecordRepository, never()).delete(any(CallRecord.class));
    }

    @Test
    void deleteCall_WithNonExistentCall_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmailAndIsActive("test@example.com", true))
                .thenReturn(Optional.of(testUser));
        when(callRecordRepository.findByUserIdAndCallId(1L, "nonexistent-id"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> callService.deleteCall("test@example.com", "nonexistent-id"));

        assertEquals("Call not found", exception.getMessage());
        verify(callRecordRepository, never()).delete(any(CallRecord.class));
    }

}