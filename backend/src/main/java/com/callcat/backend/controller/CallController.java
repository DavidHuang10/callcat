package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.RetellService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}