package com.callcat.backend.service;

import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.CallTranscript;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.CallTranscriptRepository;
import com.callcat.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TranscriptService {

    private final CallTranscriptRepository callTranscriptRepository;
    private final CallRecordRepository callRecordRepository;
    private final UserRepository userRepository;

    @Autowired
    public TranscriptService(
            CallTranscriptRepository callTranscriptRepository,
            CallRecordRepository callRecordRepository,
            UserRepository userRepository) {
        this.callTranscriptRepository = callTranscriptRepository;
        this.callRecordRepository = callRecordRepository;
        this.userRepository = userRepository;
    }

    public TranscriptResponse getTranscript(String userEmail, String callId) {
        User user = userRepository.findByEmailAndIsActive(userEmail, true)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CallRecord callRecord = callRecordRepository.findByUserIdAndCallId(user.getId(), callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (callRecord.getProviderId() == null) {
            throw new RuntimeException("Call has not started yet - no transcript available");
        }

        CallTranscript transcript = callTranscriptRepository.findByCallId(callRecord.getProviderId())
                .orElse(new CallTranscript());

        if (transcript.getCallId() == null) {
            transcript.setCallId(callRecord.getProviderId());
            transcript.setTranscriptText("");
        }

        return new TranscriptResponse(callRecord.getCallId(), transcript.getTranscriptText());
    }

    public void saveTranscript(String retellCallId, String transcriptText) {
        CallTranscript transcript = callTranscriptRepository.findByCallId(retellCallId)
                .orElse(new CallTranscript());

        transcript.setCallId(retellCallId);
        transcript.setTranscriptText(transcriptText);
        
        Instant expirationTime = Instant.now().plus(90, ChronoUnit.DAYS);
        transcript.setExpiresAt(expirationTime.getEpochSecond());

        callTranscriptRepository.save(transcript);
    }

    public void deleteTranscript(String retellCallId) {
        callTranscriptRepository.delete(retellCallId);
    }
}