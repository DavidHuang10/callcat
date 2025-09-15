package com.callcat.backend.service;

import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.repository.CallRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailureDetectionServiceTest {

    @Mock
    private CallRecordRepository callRecordRepository;

    @InjectMocks
    private FailureDetectionService failureDetectionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(failureDetectionService, "failureTimeoutMinutes", 15);
    }

    @Test
    void testDetectFailedCalls_ShouldMarkOverdueCallsAsFailed() {
        // Arrange
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = 15 * 60 * 1000L; // 15 minutes

        CallRecord overdueCall = createTestCall("overdue-call-1", "SCHEDULED");
        overdueCall.setScheduledFor(currentTime - timeoutMillis - 60000); // 16 minutes ago (overdue)

        CallRecord recentCall = createTestCall("recent-call-1", "SCHEDULED");
        recentCall.setScheduledFor(currentTime - 60000); // 1 minute ago (not overdue)

        CallRecord futureCall = createTestCall("future-call-1", "SCHEDULED");
        futureCall.setScheduledFor(currentTime + 300000); // 5 minutes in future

        List<CallRecord> scheduledCalls = Arrays.asList(overdueCall, recentCall, futureCall);

        when(callRecordRepository.findAllByStatus("SCHEDULED")).thenReturn(scheduledCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findAllByStatus("SCHEDULED");

        // Verify that only the overdue call was updated and saved
        verify(callRecordRepository, times(1)).save(any(CallRecord.class));

        // Verify that the overdue call was marked as failed (COMPLETED with dialSuccessful=false)
        verify(callRecordRepository).save(argThat(call ->
            call.getCallId().equals("overdue-call-1") &&
            "COMPLETED".equals(call.getStatus()) &&
            call.getDialSuccessful() == Boolean.FALSE &&
            call.getCompletedAt() != null
        ));
    }

    @Test
    void testDetectFailedCalls_ShouldNotMarkRecentCallsAsFailed() {
        // Arrange
        long currentTime = System.currentTimeMillis();

        CallRecord recentCall = createTestCall("recent-call-1", "SCHEDULED");
        recentCall.setScheduledFor(currentTime - 60000); // 1 minute ago (not overdue)

        List<CallRecord> scheduledCalls = Arrays.asList(recentCall);

        when(callRecordRepository.findAllByStatus("SCHEDULED")).thenReturn(scheduledCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findAllByStatus("SCHEDULED");

        // Verify that no calls were saved (no failures detected)
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void testDetectFailedCalls_ShouldHandleCallsWithNullScheduledFor() {
        // Arrange
        CallRecord callWithNullScheduledFor = createTestCall("null-scheduled-call", "SCHEDULED");
        callWithNullScheduledFor.setScheduledFor(null);

        List<CallRecord> scheduledCalls = Arrays.asList(callWithNullScheduledFor);

        when(callRecordRepository.findAllByStatus("SCHEDULED")).thenReturn(scheduledCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findAllByStatus("SCHEDULED");

        // Verify that no calls were saved (call with null scheduledFor should be skipped)
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void testDetectFailedCalls_ShouldHandleExactTimeoutBoundary() {
        // Arrange
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = 15 * 60 * 1000L; // 15 minutes

        // Call scheduled exactly at timeout boundary (should NOT be marked as failed)
        CallRecord boundaryCall = createTestCall("boundary-call", "SCHEDULED");
        boundaryCall.setScheduledFor(currentTime - timeoutMillis + 1000); // 1 second before timeout

        List<CallRecord> scheduledCalls = Arrays.asList(boundaryCall);

        when(callRecordRepository.findAllByStatus("SCHEDULED")).thenReturn(scheduledCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findAllByStatus("SCHEDULED");

        // Verify that no calls were saved (just before boundary should not fail)
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void testDetectFailedCalls_ShouldMarkMultipleOverdueCalls() {
        // Arrange
        long currentTime = System.currentTimeMillis();
        long timeoutMillis = 15 * 60 * 1000L; // 15 minutes

        CallRecord overdueCall1 = createTestCall("overdue-1", "SCHEDULED");
        overdueCall1.setScheduledFor(currentTime - timeoutMillis - 60000); // 16 minutes ago

        CallRecord overdueCall2 = createTestCall("overdue-2", "SCHEDULED");
        overdueCall2.setScheduledFor(currentTime - timeoutMillis - 120000); // 17 minutes ago

        List<CallRecord> scheduledCalls = Arrays.asList(overdueCall1, overdueCall2);

        when(callRecordRepository.findAllByStatus("SCHEDULED")).thenReturn(scheduledCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findAllByStatus("SCHEDULED");

        // Verify that both calls were saved
        verify(callRecordRepository, times(2)).save(any(CallRecord.class));
    }

    @Test
    void testDetectAndMarkFailedCalls_ShouldReturnCorrectCount() {
        // Arrange
        CallRecord scheduledCall1 = createTestCall("scheduled-1", "SCHEDULED");
        CallRecord scheduledCall2 = createTestCall("scheduled-2", "SCHEDULED");

        // Mock for detectAndMarkFailedCalls method which calls findAllByStatus twice
        when(callRecordRepository.findAllByStatus("SCHEDULED"))
                .thenReturn(Arrays.asList(scheduledCall1, scheduledCall2)) // Initial count
                .thenReturn(Arrays.asList(scheduledCall1, scheduledCall2)) // For detectFailedCalls() - no failures in this test
                .thenReturn(Arrays.asList(scheduledCall1, scheduledCall2)); // Final count

        // Act
        int failedCount = failureDetectionService.detectAndMarkFailedCalls();

        // Assert
        assertEquals(0, failedCount); // No failures expected since calls are not overdue
        verify(callRecordRepository, times(3)).findAllByStatus("SCHEDULED");
    }

    @Test
    void testDetectFailedCalls_ShouldHandleEmptyScheduledCallsList() {
        // Arrange
        when(callRecordRepository.findAllByStatus("SCHEDULED")).thenReturn(Arrays.asList());

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findAllByStatus("SCHEDULED");
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    private CallRecord createTestCall(String callId, String status) {
        CallRecord call = new CallRecord();
        call.setCallId(callId);
        call.setUserId("test-user-1");
        call.setStatus(status);
        call.setScheduledFor(System.currentTimeMillis());
        call.setCalleeName("Test Caller");
        call.setPhoneNumber("+15551234567");
        call.setSubject("Test Call");
        call.setPrompt("Test prompt");
        call.setCreatedAt(System.currentTimeMillis());
        call.setUpdatedAt(System.currentTimeMillis());
        return call;
    }
}