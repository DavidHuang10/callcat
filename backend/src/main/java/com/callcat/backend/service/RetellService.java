package com.callcat.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RetellService {
    
    private static final Logger logger = LoggerFactory.getLogger(RetellService.class);
    
    @Value("${retell.api.key:}")
    private String retellApiKey;
    
    @Value("${retell.api.url:https://api.retellai.com}")
    private String retellApiUrl;
    
    @Value("${retell.agent.id:}")
    private String defaultAgentId;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public RetellService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public String startPhoneCall(String callId, String phoneNumber, String callerNumber, String prompt, Map<String, Object> metadata) {
        try {
            String url = retellApiUrl + "/create-phone-call";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from_number", callerNumber);
            requestBody.put("to_number", phoneNumber);
            requestBody.put("agent_id", defaultAgentId);
            
            // Add our callId to metadata so Retell sends it back in webhooks
            Map<String, Object> callMetadata = new HashMap<>();
            callMetadata.put("callId", callId);
            if (metadata != null) {
                callMetadata.putAll(metadata);
            }
            requestBody.put("metadata", callMetadata);
            
            // Add custom prompt if provided
            if (prompt != null && !prompt.trim().isEmpty()) {
                Map<String, Object> dynamicVariables = new HashMap<>();
                dynamicVariables.put("custom_prompt", prompt);
                requestBody.put("retell_llm_dynamic_variables", dynamicVariables);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(retellApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("Starting phone call via Retell for callId: {}", callId);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String retellCallId = responseJson.get("call_id").asText();
                
                logger.info("Successfully started Retell call {} for callId {}", retellCallId, callId);
                return retellCallId;
            } else {
                throw new RuntimeException("Failed to start call via Retell: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error starting phone call via Retell for callId: {}", callId, e);
            throw new RuntimeException("Failed to start phone call: " + e.getMessage(), e);
        }
    }
    
    public void cancelCall(String retellCallId) {
        try {
            String url = retellApiUrl + "/cancel-call/" + retellCallId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(retellApiKey);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            logger.info("Cancelling Retell call: {}", retellCallId);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully cancelled Retell call: {}", retellCallId);
            } else {
                logger.warn("Failed to cancel Retell call {}: {}", retellCallId, response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error cancelling Retell call: {}", retellCallId, e);
        }
    }
    
    public boolean isConfigured() {
        return retellApiKey != null && !retellApiKey.trim().isEmpty() 
               && defaultAgentId != null && !defaultAgentId.trim().isEmpty();
    }
}