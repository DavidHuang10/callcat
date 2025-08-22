package com.callcat.backend.service;

import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.CallTranscript;
import com.callcat.backend.repository.CallRecordRepository;
import com.callcat.backend.repository.CallTranscriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TranscriptService {

    private final CallTranscriptRepository callTranscriptRepository;
    private final CallRecordRepository callRecordRepository;

    @Autowired
    public TranscriptService(
            CallTranscriptRepository callTranscriptRepository,
            CallRecordRepository callRecordRepository) {
        this.callTranscriptRepository = callTranscriptRepository;
        this.callRecordRepository = callRecordRepository;
    }

    public TranscriptResponse getTranscript(String callId) {
    CallRecord callRecord = callRecordRepository.findByCallId(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        if (callRecord.getProviderId() == null) {
            throw new RuntimeException("Call has not started yet - no transcript available");
        }

        CallTranscript transcript = callTranscriptRepository.findByProviderId(callRecord.getProviderId())
                .orElse(new CallTranscript());

        if (transcript.getProviderId() == null) {
            transcript.setProviderId(callRecord.getProviderId());
            transcript.setTranscriptText("");
        }

        return new TranscriptResponse(callRecord.getProviderId(), transcript.getTranscriptText());
    }

    public void saveTranscript(String retellCallId, String transcriptText) {
        CallTranscript transcript = callTranscriptRepository.findByProviderId(retellCallId)
                .orElse(new CallTranscript());

        transcript.setProviderId(retellCallId);
        transcript.setTranscriptText(transcriptText);
        
        Instant expirationTime = Instant.now().plus(90, ChronoUnit.DAYS);
        transcript.setExpiresAt(expirationTime.getEpochSecond());

        callTranscriptRepository.save(transcript);
    }
}