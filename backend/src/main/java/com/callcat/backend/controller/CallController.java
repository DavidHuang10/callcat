package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.RetellService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallController {
    
    private final CallService callService;
    private final RetellService retellService;
    
    @Value("${lambda.api.key}")
    private String expectedApiKey;
    
    public CallController(CallService callService, RetellService retellService) {
        this.callService = callService;
        this.retellService = retellService;
    }
    
    @PostMapping
    public ResponseEntity<?> createCall(
            Authentication authentication,
            @Valid @RequestBody CallRequest request) {
        try {
            String email = authentication.getName();
            CallResponse response = callService.createCall(email, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getCalls(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            String email = authentication.getName();
            
            if (limit > 100) {
                return ResponseEntity.badRequest().body(new ApiResponse("Limit cannot exceed 100", false));
            }
            
            CallListResponse response = callService.getCalls(email, status, limit);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @GetMapping("/{callId}")
    public ResponseEntity<?> getCall(
            Authentication authentication,
            @PathVariable String callId) {
        try {
            CallResponse response = callService.getCall(callId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @PutMapping("/{callId}")
    public ResponseEntity<?> updateCall(
            Authentication authentication,
            @PathVariable String callId,
            @Valid @RequestBody UpdateCallRequest request) {
        try {
            CallResponse response = callService.updateCall(callId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @DeleteMapping("/{callId}")
    public ResponseEntity<?> deleteCall(
            Authentication authentication,
            @PathVariable String callId) {
        try {
            callService.deleteCall(callId);
            return ResponseEntity.ok(new ApiResponse("Call deleted successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @PostMapping("/instant")
    public ResponseEntity<?> createInstantCall(
            Authentication authentication,
            @Valid @RequestBody CallRequest request) {
        try {
            String email = authentication.getName();
            CallResponse response = callService.createInstantCall(email, request);
            
            // Immediately trigger the call (no EventBridge/Lambda)
            retellService.makeCall(response.getCallId());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    @PostMapping("/{callId}/trigger")
    public ResponseEntity<?> triggerCall(
            @PathVariable String callId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        try {
            if (!isValidApiKey(apiKey)) {
                return ResponseEntity.status(401).body(new ApiResponse("Unauthorized", false));
            }
            
            CallResponse call = callService.getCall(callId);
            if (!"SCHEDULED".equals(call.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse("Call is not scheduled", false));
            }
            
            retellService.makeCall(callId);
            return ResponseEntity.ok(new ApiResponse("Call triggered successfully", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
    
    private boolean isValidApiKey(String providedKey) {
        return expectedApiKey != null && expectedApiKey.equals(providedKey);
    }

    @PostMapping("/demo")
    public ResponseEntity<?> createDemoCall(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse("Phone number is required", false));
            }

            // Normalize phone number to E.164 format
            String normalizedPhone = normalizePhoneNumber(phoneNumber);

            // Create demo call request with hardcoded values
            CallRequest demoRequest = new CallRequest();
            demoRequest.setCalleeName("Demo User");
            demoRequest.setPhoneNumber(normalizedPhone);
            demoRequest.setSubject("CallCat Demo");
            demoRequest.setPrompt("Hello! This is CallCat, an AI phone assistant that automates routine calls. You can schedule calls to anyone, customize the AI's voice and message, and get full transcripts. CallCat saves you time on restaurant reservations, appointment confirmations, and follow-ups. Thanks for trying our demo!");
            demoRequest.setAiLanguage("en");
            demoRequest.setVoiceId("default");

            // Create instant call for demo account
            CallResponse response = callService.createInstantCall("demo@call-cat.com", demoRequest);

            // Immediately trigger the call
            retellService.makeCall(response.getCallId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }

    private String normalizePhoneNumber(String phone) {
        // Remove all non-digit characters
        String digits = phone.replaceAll("[^0-9]", "");

        // Handle common US formats
        if (digits.length() == 10) {
            return "+1" + digits;
        }
        if (digits.length() == 11 && digits.startsWith("1")) {
            return "+" + digits;
        }

        // For international numbers or other formats, just prepend +
        if (!digits.isEmpty() && !phone.startsWith("+")) {
            return "+" + digits;
        }

        return digits.isEmpty() ? phone : "+" + digits;
    }
}