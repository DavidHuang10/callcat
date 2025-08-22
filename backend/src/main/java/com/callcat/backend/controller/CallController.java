package com.callcat.backend.controller;

import com.callcat.backend.dto.*;
import com.callcat.backend.entity.CallRecord;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.UserRepository;
import com.callcat.backend.service.CallService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calls")
public class CallController {
    
    private final CallService callService;
    private final UserRepository userRepository;
    
    public CallController(CallService callService, UserRepository userRepository) {
        this.callService = callService;
        this.userRepository = userRepository;
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
            String email = authentication.getName();
            User user = userRepository.findByEmailAndIsActive(email, true)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!callService.isCallOwner(user.getId().toString(), callId)) {
                throw new RuntimeException("Access denied");
            }
            
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
            @Valid @RequestBody CallRequest request) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndIsActive(email, true)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!callService.isCallOwner(user.getId().toString(), callId)) {
                throw new RuntimeException("Access denied");
            }
            
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
            String email = authentication.getName();
            User user = userRepository.findByEmailAndIsActive(email, true)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!callService.isCallOwner(user.getId().toString(), callId)) {
                throw new RuntimeException("Access denied");
            }
            
            callService.deleteCall(callId);
            return ResponseEntity.ok(new ApiResponse("Call deleted successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
}