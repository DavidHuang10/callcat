package com.callcat.backend.controller;

import com.callcat.backend.service.CallService;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.service.LiveTranscriptService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final CallService callService;
    private final LiveTranscriptService liveTranscriptService;
    private final ObjectMapper objectMapper;

    public WebhookController(CallService callService, LiveTranscriptService liveTranscriptService, ObjectMapper objectMapper) {
        this.callService = callService;
        this.liveTranscriptService = liveTranscriptService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/retell")
    public ResponseEntity<Void> handleRetellWebhook(@RequestBody JsonNode payload) {
        try {
            String event = payload.get("event").asText();
            JsonNode callInfo = payload.get("call");
            String providerId = callInfo.get("call_id").asText();
            
            // Try to get callId from metadata for better logging
            String callId = "unknown";
            try {
                JsonNode metadata = callInfo.get("metadata");
                if (metadata != null && metadata.has("callId")) {
                    callId = metadata.get("callId").asText();
                }
            } catch (Exception e) {
                // Metadata might not exist, that's okay
            }

            logger.info("🔔 WEBHOOK [{}]: providerId={} | callId={} | event={}", 
                       event.toUpperCase(), providerId, callId, event);

            switch (event) {
                case "call_started":
                    handleCallStarted(callInfo);
                    break;
                case "call_ended":
                    handleCallEnded(callInfo);
                    break;
                case "call_analyzed":
                    handleCallAnalyzed(callInfo);
                    break;
                default:
                    logger.warn("Unknown event received: {}", event);
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error processing Retell webhook", e);
            return ResponseEntity.noContent().build(); // Still return 204 to avoid retries
        }
    }

    private void handleCallStarted(JsonNode callInfo) {
        String providerId = callInfo.get("call_id").asText();

        try {
            CallRecord callRecord = callService.findCallByProviderId(providerId);
            callRecord.setDialSuccessful(true);
            callRecord.setRetellCallData(objectMapper.writeValueAsString(callInfo));
            
            callService.saveCallRecord(callRecord);
            
            // Live transcript polling temporarily disabled (Retell doesn't provide transcript data until CALL_ANALYZED)
            // liveTranscriptService.startPolling(providerId);
            
            logger.info("✅ CALL STARTED: callId={} | providerId={} | dialSuccess=true", 
                       callRecord.getCallId(), providerId);
        } catch (Exception e) {
            logger.error("Failed to update call status for provider ID {}", providerId, e);
        }
    }

    private void handleCallEnded(JsonNode callInfo) {
        String providerId = callInfo.get("call_id").asText();
        Long endTimestamp = callInfo.get("end_timestamp").asLong();

        try {
            CallRecord callRecord = callService.findCallByProviderId(providerId);
            callRecord.setStatus("COMPLETED");
            callRecord.setCompletedAt(endTimestamp);
            callRecord.setRetellCallData(objectMapper.writeValueAsString(callInfo));
            
            callService.saveCallRecord(callRecord);
            
            // Live transcript polling temporarily disabled (Retell doesn't provide transcript data until CALL_ANALYZED)
            // liveTranscriptService.stopPolling(providerId);
            
            logger.info("🏁 CALL ENDED: callId={} | providerId={} | status=COMPLETED | endTime={}", 
                       callRecord.getCallId(), providerId, endTimestamp);
        } catch (Exception e) {
            logger.error("Failed to process call end for provider ID {}", providerId, e);
        }
    }

    private void handleCallAnalyzed(JsonNode callInfo) {
        String providerId = callInfo.get("call_id").asText();

        try {
            CallRecord callRecord = callService.findCallByProviderId(providerId);
            callRecord.setCallAnalyzed(true);
            callRecord.setRetellCallData(objectMapper.writeValueAsString(callInfo));
            
            callService.saveCallRecord(callRecord);
            logger.info("📊 CALL ANALYZED: callId={} | providerId={} | analyzed=true", 
                       callRecord.getCallId(), providerId);
        } catch (Exception e) {
            logger.error("Failed to process call analysis for provider ID {}", providerId, e);
        }
    }
}