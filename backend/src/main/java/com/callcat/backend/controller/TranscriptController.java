package com.callcat.backend.controller;

import com.callcat.backend.dto.ApiResponse;
import com.callcat.backend.dto.TranscriptResponse;
import com.callcat.backend.service.TranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calls")
public class TranscriptController {
    
    private final TranscriptService transcriptService;
    
    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }
    
    @GetMapping("/{callId}/transcript")
    public ResponseEntity<?> getTranscript(
            Authentication authentication,
            @PathVariable String callId) {
        try {
            String email = authentication.getName();
            TranscriptResponse response = transcriptService.getTranscript(email, callId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), false));
        }
    }
}