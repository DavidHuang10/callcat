package com.callcat.backend.dto;

public class TranscriptResponse {
    
    private String callId;
    private String transcriptText;

    public TranscriptResponse() {}

    public TranscriptResponse(String callId, String transcriptText) {
        this.callId = callId;
        this.transcriptText = transcriptText;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getTranscriptText() {
        return transcriptText;
    }

    public void setTranscriptText(String transcriptText) {
        this.transcriptText = transcriptText;
    }
}