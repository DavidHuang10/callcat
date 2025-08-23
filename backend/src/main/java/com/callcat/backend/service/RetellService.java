package com.callcat.backend.service;

import com.callcat.backend.dto.CallRequest;
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
    
    public RetellService(UserService userService, CallService callService) {
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
        this.callService = callService;
    }
    
    @PostConstruct
    private void initializeRestClient() {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * Generic POST request method
     * Takes a Map as request body, converts to JSON automatically
     */
    public CallResponse makeCall(String callId) {
        try {
            // Get the call record from database
            CallRecord callRecord = callService.findCallByCallId(callId);
            
            // Get user preferences to fetch system prompt  
            String systemPrompt = userService.getUserPreferences(callRecord.getUserId()).getSystemPrompt();
            
            // Build request body for Retell API
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

            String timeInfo = "The current date is: " + LocalDate.now().toString();
            dynamicVariables.put("time_info", timeInfo);

            requestBody.put("retell_llm_dynamic_variables", dynamicVariables);
            
            // Add metadata with our internal callId for webhook identification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("callId", callId);
            requestBody.put("metadata", metadata);

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
            callService.saveCallRecord(callRecord);

            return response;
            
        } catch (Exception e) {
            logger.error("Retell API call failed for callId {}: {}", callId, e.getMessage());
            throw new RuntimeException("Failed to create call", e);
        }
    }
    
    /**
     * Convert Retell API response and CallRecord to CallResponse DTO
     */
    private CallResponse convertToCallResponse(CallRecord callRecord, JsonNode retellResponse) {
        CallResponse response = new CallResponse();
        
        // Copy all matching fields from CallRecord to CallResponse using BeanUpdateUtils
        BeanUpdateUtils.copyNonNullProperties(callRecord, response);
        
        response.setProviderId(retellResponse.get("call_id").asText());
        response.setCallerNumber(phoneNumber); // Our configured phone number

        return response;
    }
}