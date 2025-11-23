package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.RetellService;
import com.callcat.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/calls")
public class CallController {

    private final CallService callService;
    private final RetellService retellService;
    private final UserService userService;

    @Value("${lambda.api.key}")
    private String expectedApiKey;

    public CallController(CallService callService, RetellService retellService, UserService userService) {
        this.callService = callService;
        this.retellService = retellService;
        this.userService = userService;
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
        CallService.InstantCallResult result = null;
        try {
            String email = authentication.getName();
            result = callService.createInstantCall(email, request);

            // Get user preferences to pass to Retell service
            String systemPrompt = userService.getUserPreferences(email).getSystemPrompt();

            // Immediately trigger the call using the already-loaded CallRecord
            // RetellService will save the record with providerId in one atomic write
            retellService.makeCall(result.getCallRecord(), systemPrompt);

            return ResponseEntity.ok(result.toCallResponse());
        } catch (IllegalArgumentException e) {
            // Save the call record with FAILED status if it was created but Retell call failed
            if (result != null) {
                CallRecord failedCall = result.getCallRecord();
                failedCall.setStatus("FAILED");
                failedCall.setCompletedAt(System.currentTimeMillis());
                failedCall.setDialSuccessful(false);
                callService.saveCallRecord(failedCall);
            }
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            // Save the call record with FAILED status if it was created but Retell call failed
            if (result != null) {
                CallRecord failedCall = result.getCallRecord();
                failedCall.setStatus("FAILED");
                failedCall.setCompletedAt(System.currentTimeMillis());
                failedCall.setDialSuccessful(false);
                callService.saveCallRecord(failedCall);
            }
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
            String prompt = request.get("prompt");

            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse("Phone number is required", false));
            }

            // Normalize phone number to E.164 format
            String normalizedPhone = normalizePhoneNumber(phoneNumber);

            // Create temporary CallRecord (not saved to database - demo calls are ephemeral)
            CallRecord tempRecord = new CallRecord();
            tempRecord.setCallId(UUID.randomUUID().toString());
            tempRecord.setPhoneNumber(normalizedPhone);
            tempRecord.setCalleeName("Demo User");
            tempRecord.setSubject("CallCat Demo");
            
            if (prompt != null && !prompt.trim().isEmpty()) {
                tempRecord.setPrompt(prompt);
            } else {
                tempRecord.setPrompt("You are a sales representative for CallCat, a service that allows you to schedule and automate phone calls. It can be useful for routine calls, follow-ups, and other tasks. Be firm with sales, but friendly with the customer. Don't get off track.");
            }
            tempRecord.setAiLanguage("en");
            tempRecord.setVoiceId("default");

            // Call Retell API directly without saving to database
            // No user account needed - demo calls are completely ephemeral
            retellService.makeCall(tempRecord, null);

            return ResponseEntity.ok(new ApiResponse("Demo call initiated successfully", true));
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