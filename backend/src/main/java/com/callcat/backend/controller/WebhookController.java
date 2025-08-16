package com.callcat.backend.controller;

import com.callcat.backend.dto.ApiResponse;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.TranscriptService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    private final CallService callService;
    private final TranscriptService transcriptService;
    
    public WebhookController(CallService callService, TranscriptService transcriptService) {
        this.callService = callService;
        this.transcriptService = transcriptService;
    }
    
    @PostMapping("/retell")
    public ResponseEntity<Void> handleRetellWebhook(@RequestBody JsonNode payload) {
        try {
            String callId = payload.get("call_id").asText();
            String callStatus = payload.get("call_status").asText();
            
            logger.info("Received Retell webhook for call {} with status {}", callId, callStatus);
            
            // Handle different call statuses
            switch (callStatus.toLowerCase()) {
                case "registered":
                case "in_progress":
                    handleCallStarted(payload);
                    break;
                case "ended":
                case "completed":
                    handleCallEnded(payload);
                    break;
                case "analyzed":
                    handleCallAnalyzed(payload);
                    break;
                default:
                    logger.warn("Unknown call status received: {}", callStatus);
            }
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            logger.error("Error processing Retell webhook", e);
            return ResponseEntity.noContent().build(); // Still return 204 to avoid retries
        }
    }
    
    private void handleCallStarted(JsonNode payload) {
        String retellCallId = payload.get("call_id").asText();
        Long startTimestamp = payload.has("start_timestamp") ? payload.get("start_timestamp").asLong() : null;
        
        try {
            String callId = extractCallIdFromMetadata(payload);
            if (callId != null) {
                callService.updateCallStatusWithRetellData(callId, "IN_PROGRESS", startTimestamp, null, retellCallId);
                logger.info("Updated call {} (Retell: {}) to IN_PROGRESS", callId, retellCallId);
            } else {
                logger.warn("No callId found in metadata for Retell call {}", retellCallId);
            }
        } catch (Exception e) {
            logger.error("Failed to update call status for Retell call {}", retellCallId, e);
        }
    }
    
    private void handleCallEnded(JsonNode payload) {
        String retellCallId = payload.get("call_id").asText();
        Long endTimestamp = payload.has("end_timestamp") ? payload.get("end_timestamp").asLong() : null;
        
        try {
            String callId = extractCallIdFromMetadata(payload);
            if (callId != null) {
                callService.updateCallStatusWithRetellData(callId, "COMPLETED", null, endTimestamp, retellCallId);
                
                // Store transcript if available
                if (payload.has("transcript") && !payload.get("transcript").isNull()) {
                    String transcript = payload.get("transcript").asText();
                    transcriptService.saveTranscript(retellCallId, transcript);
                }
                
                // Store the complete Retell response data
                callService.updateRetellCallData(callId, payload);
                
                logger.info("Updated call {} (Retell: {}) to COMPLETED", callId, retellCallId);
            } else {
                logger.warn("No callId found in metadata for Retell call {}", retellCallId);
            }
        } catch (Exception e) {
            logger.error("Failed to process call end for Retell call {}", retellCallId, e);
        }
    }
    
    private void handleCallAnalyzed(JsonNode payload) {
        String retellCallId = payload.get("call_id").asText();
        
        try {
            String callId = extractCallIdFromMetadata(payload);
            if (callId != null) {
                // Update transcript with latest version
                if (payload.has("transcript") && !payload.get("transcript").isNull()) {
                    String transcript = payload.get("transcript").asText();
                    transcriptService.saveTranscript(retellCallId, transcript);
                }
                
                // Store the complete analyzed data
                callService.updateRetellCallData(callId, payload);
                
                logger.info("Updated call {} (Retell: {}) with analysis data", callId, retellCallId);
            } else {
                logger.warn("No callId found in metadata for Retell call {}", retellCallId);
            }
        } catch (Exception e) {
            logger.error("Failed to process call analysis for Retell call {}", retellCallId, e);
        }
    }
    
    private String extractCallIdFromMetadata(JsonNode payload) {
        // Look for our callId in the metadata that we send to Retell
        if (payload.has("metadata") && payload.get("metadata").has("callId")) {
            return payload.get("metadata").get("callId").asText();
        }
        // Fallback: look in retell_llm_dynamic_variables if we store it there
        if (payload.has("retell_llm_dynamic_variables") && payload.get("retell_llm_dynamic_variables").has("callId")) {
            return payload.get("retell_llm_dynamic_variables").get("callId").asText();
        }
        return null;
    }
}