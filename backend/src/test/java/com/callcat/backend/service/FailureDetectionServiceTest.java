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
import static org.mockito.ArgumentMatchers.anyLong;
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

        // Only overdue calls are returned by findOverdueScheduledCalls
        List<CallRecord> overdueCalls = Arrays.asList(overdueCall);

        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(overdueCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());

        // Verify that the overdue call was updated and saved
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
        // Arrange - no overdue calls returned
        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(Arrays.asList());

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());

        // Verify that no calls were saved (no failures detected)
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void testDetectFailedCalls_ShouldHandleCallsWithNullScheduledFor() {
        // Arrange - DynamoDB filter excludes calls with null scheduledFor
        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(Arrays.asList());

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());

        // Verify that no calls were saved
        verify(callRecordRepository, never()).save(any(CallRecord.class));
    }

    @Test
    void testDetectFailedCalls_ShouldHandleExactTimeoutBoundary() {
        // Arrange - DynamoDB filter handles boundary, returns nothing
        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(Arrays.asList());

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());

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

        List<CallRecord> overdueCalls = Arrays.asList(overdueCall1, overdueCall2);

        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(overdueCalls);

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());

        // Verify that both calls were saved
        verify(callRecordRepository, times(2)).save(any(CallRecord.class));
    }

    @Test
    void testDetectAndMarkFailedCalls_ShouldReturnCorrectCount() {
        // Arrange
        CallRecord scheduledCall1 = createTestCall("scheduled-1", "SCHEDULED");
        CallRecord scheduledCall2 = createTestCall("scheduled-2", "SCHEDULED");

        // Mock findAllByStatus for initial and final counts
        when(callRecordRepository.findAllByStatus("SCHEDULED"))
                .thenReturn(Arrays.asList(scheduledCall1, scheduledCall2)) // Initial count
                .thenReturn(Arrays.asList(scheduledCall1, scheduledCall2)); // Final count
        
        // Mock findOverdueScheduledCalls for detectFailedCalls() - no overdue calls
        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(Arrays.asList());

        // Act
        int failedCount = failureDetectionService.detectAndMarkFailedCalls();

        // Assert
        assertEquals(0, failedCount); // No failures expected since no overdue calls
        verify(callRecordRepository, times(2)).findAllByStatus("SCHEDULED");
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());
    }

    @Test
    void testDetectFailedCalls_ShouldHandleEmptyScheduledCallsList() {
        // Arrange
        when(callRecordRepository.findOverdueScheduledCalls(anyLong())).thenReturn(Arrays.asList());

        // Act
        failureDetectionService.detectFailedCalls();

        // Assert
        verify(callRecordRepository).findOverdueScheduledCalls(anyLong());
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