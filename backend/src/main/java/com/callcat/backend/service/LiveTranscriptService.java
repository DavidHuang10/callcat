package com.callcat.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class LiveTranscriptService {
    
    private static final Logger logger = LoggerFactory.getLogger(LiveTranscriptService.class);
    
    private final RetellService retellService;
    private final TranscriptService transcriptService;
    private final ScheduledExecutorService executorService;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> activePolls;
    
    public LiveTranscriptService(RetellService retellService, TranscriptService transcriptService) {
        this.retellService = retellService;
        this.transcriptService = transcriptService;
        this.executorService = Executors.newScheduledThreadPool(10); // Max 10 concurrent calls
        this.activePolls = new ConcurrentHashMap<>();
    }
    
    public void startPolling(String providerId) {
        if (activePolls.containsKey(providerId)) {
            logger.warn("Polling already active for providerId: {}", providerId);
            return;
        }
        
        logger.info("Starting live transcript polling for providerId: {}", providerId);
        
        ScheduledFuture<?> pollingTask = executorService.scheduleAtFixedRate(
            () -> pollTranscript(providerId),
            0, // Start immediately
            3, // Poll every 3 seconds
            TimeUnit.SECONDS
        );
        
        activePolls.put(providerId, pollingTask);
        
        // Auto-stop after 10 minutes
        executorService.schedule(() -> {
            if (activePolls.containsKey(providerId)) {
                logger.info("Auto-stopping polling for providerId {} after 10 minutes", providerId);
                stopPolling(providerId);
            }
        }, 10, TimeUnit.MINUTES);
    }
    
    public void stopPolling(String providerId) {
        ScheduledFuture<?> pollingTask = activePolls.remove(providerId);
        if (pollingTask != null) {
            pollingTask.cancel(false);
            logger.info("Stopped live transcript polling for providerId: {}", providerId);
        }
    }
    
    private void pollTranscript(String providerId) {
        try {
            JsonNode callData = retellService.getCall(providerId);
            JsonNode transcriptNode = callData.get("transcript");
            
            if (transcriptNode != null && !transcriptNode.isNull()) {
                String transcript = transcriptNode.asText();
                if (transcript != null && !transcript.trim().isEmpty()) {
                    transcriptService.updateLiveTranscript(providerId, transcript);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to poll transcript for providerId {}: {}", providerId, e.getMessage());
            // Continue polling despite errors - let timeout handle persistent failures
        }
    }
}