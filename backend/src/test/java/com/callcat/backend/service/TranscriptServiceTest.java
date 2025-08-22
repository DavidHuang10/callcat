package com.callcat.backend.service;

import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.CallTranscript;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.CallTranscriptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceTest {

    @Mock
    private CallTranscriptRepository callTranscriptRepository;

    @Mock
    private CallRecordRepository callRecordRepository;

    @InjectMocks
    private TranscriptService transcriptService;

    private CallRecord testCall;
    private CallTranscript testTranscript;

    @BeforeEach
    void setUp() {
        testCall = new CallRecord();
        testCall.setUserId("1");
        testCall.setCallId("user-call-id");
        testCall.setProviderId("retell-call-id-123");
        testCall.setCalleeName("John Doe");
        testCall.setStatus("COMPLETED");

        testTranscript = new CallTranscript();
        testTranscript.setProviderId("retell-call-id-123");
        testTranscript.setTranscriptText("Agent: Hello! How can I help you today?\nUser: I'd like to schedule an appointment.");
        testTranscript.setExpiresAt(Instant.now().plus(90, ChronoUnit.DAYS).getEpochSecond());
    }

    @Test
    void getTranscript_WithExistingTranscript_ShouldReturnTranscript() {
        // Arrange
        when(callRecordRepository.findByCallId("user-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callTranscriptRepository.findByProviderId("retell-call-id-123"))
                .thenReturn(Optional.of(testTranscript));

        // Act
        TranscriptResponse result = transcriptService.getTranscript("user-call-id");

        // Assert
        assertNotNull(result);
        assertEquals("retell-call-id-123", result.getProviderId());
        assertEquals("Agent: Hello! How can I help you today?\nUser: I'd like to schedule an appointment.", 
                result.getTranscriptText());

        verify(callRecordRepository).findByCallId("user-call-id");
        verify(callTranscriptRepository).findByProviderId("retell-call-id-123");
    }

    @Test
    void getTranscript_WithNoExistingTranscript_ShouldReturnEmptyTranscript() {
        // Arrange
        when(callRecordRepository.findByCallId("user-call-id"))
                .thenReturn(Optional.of(testCall));
        when(callTranscriptRepository.findByProviderId("retell-call-id-123"))
                .thenReturn(Optional.empty());

        // Act
        TranscriptResponse result = transcriptService.getTranscript("user-call-id");

        // Assert
        assertNotNull(result);
        assertEquals("retell-call-id-123", result.getProviderId());
        assertEquals("", result.getTranscriptText());

        verify(callRecordRepository).findByCallId("user-call-id");
        verify(callTranscriptRepository).findByProviderId("retell-call-id-123");
    }

    @Test
    void getTranscript_WithNonExistentCall_ShouldThrowException() {
        // Arrange
        when(callRecordRepository.findByCallId("nonexistent-call"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transcriptService.getTranscript("nonexistent-call"));

        assertEquals("Call not found", exception.getMessage());
        verify(callRecordRepository).findByCallId("nonexistent-call");
        verify(callTranscriptRepository, never()).findByProviderId(any());
    }

    @Test
    void getTranscript_WithCallNotStarted_ShouldThrowException() {
        // Arrange
        testCall.setProviderId(null); // Call hasn't started yet
        when(callRecordRepository.findByCallId("user-call-id"))
                .thenReturn(Optional.of(testCall));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transcriptService.getTranscript("user-call-id"));

        assertEquals("Call has not started yet - no transcript available", exception.getMessage());
        verify(callRecordRepository).findByCallId("user-call-id");
        verify(callTranscriptRepository, never()).findByProviderId(any());
    }

    @Test
    void saveTranscript_WithNewTranscript_ShouldCreateAndSave() {
        // Arrange
        String retellCallId = "retell-call-id-456";
        String transcriptText = "Agent: Welcome to our service!\nUser: Thank you!";
        
        when(callTranscriptRepository.findByProviderId(retellCallId))
                .thenReturn(Optional.empty());
        when(callTranscriptRepository.save(any(CallTranscript.class)))
                .thenReturn(testTranscript);

        // Act
        transcriptService.saveTranscript(retellCallId, transcriptText);

        // Assert
        verify(callTranscriptRepository).findByProviderId(retellCallId);
        verify(callTranscriptRepository).save(argThat(transcript -> 
            retellCallId.equals(transcript.getProviderId()) &&
            transcriptText.equals(transcript.getTranscriptText()) &&
            transcript.getExpiresAt() != null
        ));
    }

    @Test
    void saveTranscript_WithExistingTranscript_ShouldUpdate() {
        // Arrange
        String retellCallId = "retell-call-id-123";
        String newTranscriptText = "Agent: Welcome!\nUser: Hi there!\nAgent: How can I help?";
        
        when(callTranscriptRepository.findByProviderId(retellCallId))
                .thenReturn(Optional.of(testTranscript));
        when(callTranscriptRepository.save(any(CallTranscript.class)))
                .thenReturn(testTranscript);

        // Act
        transcriptService.saveTranscript(retellCallId, newTranscriptText);

        // Assert
        verify(callTranscriptRepository).findByProviderId(retellCallId);
        verify(callTranscriptRepository).save(testTranscript);
        assertEquals(newTranscriptText, testTranscript.getTranscriptText());
        assertNotNull(testTranscript.getExpiresAt());
    }

    @Test
    void saveTranscript_ShouldSetCorrectTTL() {
        // Arrange
        String retellCallId = "retell-call-id-789";
        String transcriptText = "Test transcript";
        
        when(callTranscriptRepository.findByProviderId(retellCallId))
                .thenReturn(Optional.empty());

        // Act
        transcriptService.saveTranscript(retellCallId, transcriptText);

        // Assert
        verify(callTranscriptRepository).save(argThat(transcript -> {
            long now = Instant.now().getEpochSecond();
            long ninetyDaysFromNow = Instant.now().plus(90, ChronoUnit.DAYS).getEpochSecond();
            long expiresAt = transcript.getExpiresAt();
            
            // Allow for some tolerance (within 1 hour)
            return expiresAt >= (ninetyDaysFromNow - 3600) && expiresAt <= (ninetyDaysFromNow + 3600);
        }));
    }

    @Test
    void saveTranscript_WithEmptyTranscript_ShouldSaveEmpty() {
        // Arrange
        String retellCallId = "retell-call-id-empty";
        String emptyTranscript = "";
        
        when(callTranscriptRepository.findByProviderId(retellCallId))
                .thenReturn(Optional.empty());

        // Act
        transcriptService.saveTranscript(retellCallId, emptyTranscript);

        // Assert
        verify(callTranscriptRepository).save(argThat(transcript -> 
            retellCallId.equals(transcript.getProviderId()) &&
            "".equals(transcript.getTranscriptText())
        ));
    }

    @Test
    void saveTranscript_WithLongTranscript_ShouldSaveComplete() {
        // Arrange
        String retellCallId = "retell-call-id-long";
        StringBuilder longTranscript = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longTranscript.append("Agent: This is message ").append(i).append("\n");
            longTranscript.append("User: Response to message ").append(i).append("\n");
        }
        String transcriptText = longTranscript.toString();
        
        when(callTranscriptRepository.findByProviderId(retellCallId))
                .thenReturn(Optional.empty());

        // Act
        transcriptService.saveTranscript(retellCallId, transcriptText);

        // Assert
        verify(callTranscriptRepository).save(argThat(transcript -> 
            retellCallId.equals(transcript.getProviderId()) &&
            transcriptText.equals(transcript.getTranscriptText()) &&
            transcript.getTranscriptText().length() > 10000
        ));
    }
}