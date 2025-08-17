package com.callcat.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class RetellService {
    
    private static final Logger logger = LoggerFactory.getLogger(RetellService.class);
    
    @Value("${retell.api.key:}")
    private String retellApiKey;
    
    @Value("${retell.base.url}")
    private String retellBaseUrl;
    
    @Value("${retell.phone.number}")
    private String retellPhoneNumber;
    
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    
    public RetellService(RestClient.Builder restClientBuilder) {
        this.objectMapper = new ObjectMapper();
        this.restClient = restClientBuilder
            .baseUrl(retellBaseUrl)
            .defaultHeader("Authorization", "Bearer " + retellApiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
    
    public JsonNode createCall(String phoneNumber, String systemPrompt, String taskPrompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from_number", retellPhoneNumber);
            requestBody.put("to_number", phoneNumber);
            
            // Create retell_llm_dynamic_variables with system_prompt and task_prompt
            Map<String, Object> dynamicVariables = new HashMap<>();
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                dynamicVariables.put("system_prompt", systemPrompt);
            }
            if (taskPrompt != null && !taskPrompt.trim().isEmpty()) {
                dynamicVariables.put("task_prompt", taskPrompt);
            }
            
            if (!dynamicVariables.isEmpty()) {
                requestBody.put("retell_llm_dynamic_variables", dynamicVariables);
            }
            
            logger.info("Creating call via Retell API to number: {}", phoneNumber);
            
            String responseBody = restClient.post()
                .uri("/call")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);
            
            JsonNode responseJson = objectMapper.readTree(responseBody);
            logger.info("Successfully created Retell call with ID: {}", responseJson.get("call_id").asText());
            return responseJson;
            
        } catch (Exception e) {
            logger.error("Error creating call via Retell to number: {}", phoneNumber, e);
            throw new RuntimeException("Failed to create call: " + e.getMessage(), e);
        }
    }
    
    public void cancelCall(String retellCallId) {
        try {
            logger.info("Cancelling Retell call: {}", retellCallId);
            
            restClient.post()
                .uri("/cancel-call/" + retellCallId)
                .retrieve()
                .toBodilessEntity();
            
            logger.info("Successfully cancelled Retell call: {}", retellCallId);
            
        } catch (Exception e) {
            logger.error("Error cancelling Retell call: {}", retellCallId, e);
        }
    }
    
    public boolean isConfigured() {
        return retellApiKey != null && !retellApiKey.trim().isEmpty() 
               && retellPhoneNumber != null && !retellPhoneNumber.trim().isEmpty()
               && retellBaseUrl != null && !retellBaseUrl.trim().isEmpty();
    }
}