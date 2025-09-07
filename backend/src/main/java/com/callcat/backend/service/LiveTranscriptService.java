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
    
    // Feature toggle: Temporarily disabled because Retell doesn't provide transcript data until CALL_ANALYZED
    private static final boolean LIVE_POLLING_ENABLED = false;
    
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
        if (!LIVE_POLLING_ENABLED) {
            logger.info("🚫 LIVE POLLING DISABLED: providerId={} | feature disabled until post-call transcript implementation", providerId);
            return;
        }
        
        if (activePolls.containsKey(providerId)) {
            logger.warn("Polling already active for providerId: {}", providerId);
            return;
        }
        
        logger.info("🎙️ LIVE POLLING STARTED: providerId={} | interval=3s | timeout=20min | activePolls={}", 
                   providerId, activePolls.size() + 1);
        
        ScheduledFuture<?> pollingTask = executorService.scheduleAtFixedRate(
            () -> pollTranscript(providerId),
            0, // Start immediately
            3, // Poll every 3 seconds
            TimeUnit.SECONDS
        );
        
        activePolls.put(providerId, pollingTask);
        
        // Auto-stop after 20 minutes (increased from 10)
        executorService.schedule(() -> {
            if (activePolls.containsKey(providerId)) {
                logger.warn("⏱️ TIMEOUT: Auto-stopping polling for providerId={} after 20 minutes | activePolls={}", 
                           providerId, activePolls.size() - 1);
                stopPolling(providerId);
            }
        }, 20, TimeUnit.MINUTES);
    }
    
    public void stopPolling(String providerId) {
        if (!LIVE_POLLING_ENABLED) {
            logger.info("🚫 LIVE POLLING DISABLED: providerId={} | stop requested but feature is disabled", providerId);
            return;
        }
        
        ScheduledFuture<?> pollingTask = activePolls.remove(providerId);
        if (pollingTask != null) {
            pollingTask.cancel(false);
            logger.info("🛑 LIVE POLLING STOPPED: providerId={} | remainingActive={}", 
                       providerId, activePolls.size());
        } else {
            logger.warn("⚠️ STOP POLLING FAILED: No active polling found for providerId={}", providerId);
        }
    }
    
    private void pollTranscript(String providerId) {
        try {
            logger.debug("📡 POLLING: providerId={} | fetching from Retell API...", providerId);
            JsonNode callData = retellService.getCall(providerId);
            JsonNode transcriptNode = callData.get("transcript");
            
            if (transcriptNode != null && !transcriptNode.isNull()) {
                String transcript = transcriptNode.asText();
                if (transcript != null && !transcript.trim().isEmpty()) {
                    logger.info("💬 TRANSCRIPT UPDATE: providerId={} | length={}chars | saved=true", 
                               providerId, transcript.length());
                    transcriptService.updateLiveTranscript(providerId, transcript);
                } else {
                    logger.debug("📭 EMPTY TRANSCRIPT: providerId={} | transcript=null/empty", providerId);
                }
            } else {
                logger.debug("📭 NO TRANSCRIPT NODE: providerId={} | response missing 'transcript' field", providerId);
            }
        } catch (Exception e) {
            logger.error("❌ POLLING ERROR: providerId={} | error={} | continuing...", 
                        providerId, e.getMessage());
            // Continue polling despite errors - let timeout handle persistent failures
        }
    }
}