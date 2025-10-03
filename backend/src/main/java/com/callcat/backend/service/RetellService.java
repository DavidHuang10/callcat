package com.callcat.backend.service;

import com.callcat.backend.dto.CallResponse;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.util.BeanUpdateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Boilerplate template for API service classes
 * This shows the standard pattern for making HTTP requests in Spring Boot
 */
@Service
public class RetellService {
    
    private static final Logger logger = LoggerFactory.getLogger(RetellService.class);
    
    // Configuration from application.properties
    @Value("${retell.api.key}")
    private String apiKey;
    
    @Value("${retell.base.url}")
    private String baseUrl;

    @Value("${retell.phone.number}")
    private String phoneNumber;

    // Spring beans for HTTP calls and JSON processing
    private RestClient restClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CallService callService;
    private final HttpComponentsClientHttpRequestFactory httpRequestFactory;
    
    public RetellService(UserService userService, CallService callService, HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
        this.callService = callService;
        this.httpRequestFactory = httpRequestFactory;
    }
    
    @PostConstruct
    private void initializeRestClient() {
        this.restClient = RestClient.builder()
            .requestFactory(httpRequestFactory) // Use configured HTTP client with connection pooling
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
            
        logger.info("RestClient initialized with connection pooling - max 20 total connections, 10 per route");
    }


    /**
     * Make a call using Retell API - optimized version that accepts CallRecord directly
     * This version is used for instant calls to avoid race conditions with DynamoDB eventual consistency
     *
     * @param callRecord The call record with all necessary data
     * @param systemPrompt The user's system prompt from preferences
     * @return CallResponse with updated provider information
     */
    public CallResponse makeCall(CallRecord callRecord, String systemPrompt) {
        try {
            String callId = callRecord.getCallId();
            Map<String, Object> requestBody = buildRetellRequestBody(callRecord, systemPrompt, callId);
            logger.info("Making POST request to Retell API for phone: {}", callRecord.getPhoneNumber());

            String responseBody = restClient.post()
                .uri("/create-phone-call")
                .body(requestBody)
                .retrieve()
                .body(String.class);

            logger.info("Retell API call successful");

            // Parse Retell response
            JsonNode retellResponse = objectMapper.readTree(responseBody);

            // Convert to CallResponse DTO
            CallResponse response = convertToCallResponse(callRecord, retellResponse);

            // Update the CallRecord with the providerId from Retell
            callRecord.setProviderId(retellResponse.get("call_id").asText());
            callRecord.setRetellCallData(objectMapper.writeValueAsString(retellResponse));
            
            // Only save if this has a userId (not a demo call)
            // Demo calls are ephemeral and should not be persisted
            if (callRecord.getUserId() != null) {
                callService.saveCallRecord(callRecord);
                logger.info("Updated call record with providerId: {}", callRecord.getProviderId());
            } else {
                logger.info("Demo call - skipping database save (ephemeral)");
            }

            return response;

        } catch (Exception e) {
            logger.error("Retell API call failed for callId {}: {}", callRecord.getCallId(), e.getMessage());
            throw new RuntimeException("Failed to create call", e);
        }
    }

    /**
     * Make a call using Retell API - fetches CallRecord from database
     * This version is used by Lambda/scheduled calls that only have the callId
     *
     * @param callId The ID of the call to make
     * @return CallResponse with updated provider information
     */
    public CallResponse makeCall(String callId) {
        try {
            CallRecord callRecord = callService.findCallByCallId(callId);
            String systemPrompt = userService.getUserPreferences(Long.parseLong(callRecord.getUserId())).getSystemPrompt();

            // Delegate to the optimized method
            return makeCall(callRecord, systemPrompt);

        } catch (Exception e) {
            logger.error("Retell API call failed for callId {}: {}", callId, e.getMessage());
            throw new RuntimeException("Failed to create call", e);
        }
    }

    public JsonNode getCall(String retellCallId) {
        try {
            logger.info("Getting call details from Retell API for call ID: {}", retellCallId);
            
            String responseBody = restClient.get()
                .uri("/get-call/{callId}", retellCallId)
                .retrieve()
                .body(String.class);
            
            logger.info("Retell API get call successful");
            
            return objectMapper.readTree(responseBody);
            
        } catch (Exception e) {
            logger.error("Failed to get call from Retell API for callId {}: {}", retellCallId, e.getMessage());
            throw new RuntimeException("Failed to get call details", e);
        }
    }
    
    /**
     * Build request body for Retell API call
     */
    private Map<String, Object> buildRetellRequestBody(CallRecord callRecord, String systemPrompt, String callId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from_number", phoneNumber);
        requestBody.put("to_number", callRecord.getPhoneNumber());
        
        // Add dynamic variables for LLM
        Map<String, Object> dynamicVariables = new HashMap<>();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            dynamicVariables.put("system_prompt", systemPrompt);
        }
        if (callRecord.getPrompt() != null && !callRecord.getPrompt().trim().isEmpty()) {
            dynamicVariables.put("task_prompt", callRecord.getPrompt());
        }
        String timeInfo = "The current date is: " + LocalDate.now();
        dynamicVariables.put("time_info", timeInfo);
        String langInfo = "The current language is: " + callRecord.getAiLanguage();
        dynamicVariables.put("ai_language", langInfo);
        dynamicVariables.put("voice_id", callRecord.getVoiceId());
       
        requestBody.put("retell_llm_dynamic_variables", dynamicVariables);
        
        // Add metadata with our internal callId for webhook identification
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("callId", callId);
        requestBody.put("metadata", metadata);
        
        return requestBody;
    }

    /**
     * Convert Retell API response and CallRecord to CallResponse DTO
     */
    private CallResponse convertToCallResponse(CallRecord callRecord, JsonNode retellResponse) {
        CallResponse response = new CallResponse();
        BeanUpdateUtils.copyNonNullProperties(callRecord, response);
        response.setProviderId(retellResponse.get("call_id").asText());
        response.setCallerNumber(phoneNumber);
        return response;
    }
}