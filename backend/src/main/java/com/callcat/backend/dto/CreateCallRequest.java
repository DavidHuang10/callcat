package com.callcat.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCallRequest {

    @NotBlank(message = "Callee name is required")
    @Size(max = 100, message = "Callee name must be less than 100 characters")
    private String calleeName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+1[0-9]{10}$", message = "Phone number must be in E.164 format (+1XXXXXXXXXX)")
    private String phoneNumber;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be less than 200 characters")
    private String subject;

    @NotBlank(message = "Prompt is required")
    @Size(max = 5000, message = "Prompt must be less than 5000 characters")
    private String prompt;

    private Long scheduledAt;

    @Size(max = 10, message = "AI language code must be less than 10 characters")
    private String aiLanguage;

    @Size(max = 100, message = "Voice ID must be less than 100 characters")
    private String voiceId;

    public CreateCallRequest() {}

    public String getCalleeName() {
        return calleeName;
    }

    public void setCalleeName(String calleeName) {
        this.calleeName = calleeName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getAiLanguage() {
        return aiLanguage;
    }

    public void setAiLanguage(String aiLanguage) {
        this.aiLanguage = aiLanguage;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}