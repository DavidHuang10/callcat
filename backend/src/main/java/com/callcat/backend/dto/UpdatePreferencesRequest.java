package com.callcat.backend.dto;

import jakarta.validation.constraints.Size;

public class UpdatePreferencesRequest {
    private String timezone;
    private Boolean emailNotifications;
    private String voiceId;
    
    @Size(max = 1000, message = "System prompt cannot exceed 1000 characters")
    private String systemPrompt;

    public UpdatePreferencesRequest() {}

    public UpdatePreferencesRequest(String timezone, Boolean emailNotifications, String voiceId, String systemPrompt) {
        this.timezone = timezone;
        this.emailNotifications = emailNotifications;
        this.voiceId = voiceId;
        this.systemPrompt = systemPrompt;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}