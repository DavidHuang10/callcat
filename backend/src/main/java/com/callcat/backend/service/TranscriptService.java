package com.callcat.backend.service;

import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.entity.CallTranscript;
import com.callcat.backend.repository.CallTranscriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TranscriptService {

    private final CallTranscriptRepository callTranscriptRepository;

    @Autowired
    public TranscriptService(CallTranscriptRepository callTranscriptRepository) {
        this.callTranscriptRepository = callTranscriptRepository;
    }


    public TranscriptResponse getTranscriptByProviderId(String providerId) {
        CallTranscript transcript = callTranscriptRepository.findByProviderId(providerId)
                .orElse(new CallTranscript());

        if (transcript.getProviderId() == null) {
            transcript.setProviderId(providerId);
            transcript.setTranscriptText("");
        }

        return new TranscriptResponse(providerId, transcript.getTranscriptText());
    }

    public void saveTranscript(String providerId, String transcriptText) {
        CallTranscript transcript = callTranscriptRepository.findByProviderId(providerId)
                .orElse(new CallTranscript());

        transcript.setProviderId(providerId);
        transcript.setTranscriptText(transcriptText);
        
        Instant expirationTime = Instant.now().plus(90, ChronoUnit.DAYS);
        transcript.setExpiresAt(expirationTime.getEpochSecond());

        callTranscriptRepository.save(transcript);
    }
}