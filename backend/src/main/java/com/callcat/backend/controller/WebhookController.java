package com.callcat.backend.controller;

import com.callcat.backend.dto.ApiResponse;
import com.callcat.backend.service.CallService;
import com.callcat.backend.entity.CallRecord;
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
            String event = payload.get("event").asText();
            JsonNode callInfo = payload.get("call");
            String providerId = callInfo.get("callId").asText();

            logger.info("Received Retell webhook for providerId {} with status {}", providerId, event);

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
            callRecord.setRetellCallData(callInfo.toString());
            
            callService.saveCallRecord(callRecord);
            logger.info("Updated call {} with provider ID {}", callRecord.getCallId(), providerId);
        } catch (Exception e) {
            logger.error("Failed to update call status for provider ID {}", providerId, e);
        }
    }

    private void handleCallEnded(JsonNode callInfo) {
        String providerId = callInfo.get("call_id").asText();
        Long endTimestamp = callInfo.has("end_timestamp") ? callInfo.get("end_timestamp").asLong() : null;

        try {
            CallRecord callRecord = callService.findCallByProviderId(providerId);
            callRecord.setStatus("COMPLETED");
            callRecord.setCompletedAt(endTimestamp);
            callRecord.setDialSuccessful(true);
            callRecord.setUpdatedAt(System.currentTimeMillis());
            callRecord.setRetellCallData(callInfo.toString());
            
            callService.saveCallRecord(callRecord);
            logger.info("Updated call {} (Provider: {}) to COMPLETED", callRecord.getCallId(), providerId);
        } catch (Exception e) {
            logger.error("Failed to process call end for provider ID {}", providerId, e);
        }
    }

    private void handleCallAnalyzed(JsonNode callInfo) {
        String providerId = callInfo.get("call_id").asText();

        try {
            CallRecord callRecord = callService.findCallByProviderId(providerId);
            callRecord.setCallAnalyzed(true);
            callRecord.setUpdatedAt(System.currentTimeMillis());
            callRecord.setRetellCallData(callInfo.toString());
            
            callService.saveCallRecord(callRecord);
            logger.info("Updated call {} (Provider: {}) with analysis data", callRecord.getCallId(), providerId);
        } catch (Exception e) {
            logger.error("Failed to process call analysis for provider ID {}", providerId, e);
        }
    }
}