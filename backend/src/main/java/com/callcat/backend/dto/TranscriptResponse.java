package com.callcat.backend.dto;

public class TranscriptResponse {
    
    private String providerId;
    private String transcriptText;

    public TranscriptResponse() {}

    public TranscriptResponse(String providerId, String transcriptText) {
        this.providerId = providerId;
        this.transcriptText = transcriptText;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getTranscriptText() {
        return transcriptText;
    }

    public void setTranscriptText(String transcriptText) {
        this.transcriptText = transcriptText;
    }
}