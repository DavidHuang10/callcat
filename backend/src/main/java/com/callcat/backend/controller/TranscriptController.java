package com.callcat.backend.controller;

import com.callcat.backend.dto.ApiResponse;
import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.UserRepository;
import com.callcat.backend.service.CallService;
import com.callcat.backend.service.TranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calls")
public class TranscriptController {
    
    private final TranscriptService transcriptService;
    private final CallService callService;
    private final UserRepository userRepository;
    
    public TranscriptController(TranscriptService transcriptService, CallService callService, UserRepository userRepository) {
        this.transcriptService = transcriptService;
        this.callService = callService;
        this.userRepository = userRepository;
    }
    
    @GetMapping("/{callId}/transcript")
    public ResponseEntity<?> getTranscript(
            Authentication authentication,
            @PathVariable String callId) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndIsActive(email, true)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!callService.isCallOwner(user.getId().toString(), callId)) {
                throw new RuntimeException("Access denied");
            }
            
            TranscriptResponse response = transcriptService.getTranscript(callId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
}