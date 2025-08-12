package com.callcat.backend.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class CallRecord {
    private Long userId;
    private String callId;
    private String calleeName;
    private String phoneNumber;
    private String subject;
    private String prompt;
    private String status;
    private Long scheduledAt;
    private Long callAt;
    private String aiLanguage;
    private String voiceId;
    private Long createdAt;
    private Long updatedAt;
    
    // Optional fields after completion
    private String summary;
    private Integer durationSec;
    private String outcome;
    private String transcriptUrl;
    private String audioRecordingUrl;
    private Long completedAt;

    public CallRecord() {}

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = {"upcoming-calls-index", "completed-calls-index"})
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @DynamoDbSortKey
    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDbSecondarySortKey(indexNames = "upcoming-calls-index")
    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Long getCallAt() {
        return callAt;
    }

    public void setCallAt(Long callAt) {
        this.callAt = callAt;
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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getTranscriptUrl() {
        return transcriptUrl;
    }

    public void setTranscriptUrl(String transcriptUrl) {
        this.transcriptUrl = transcriptUrl;
    }

    public String getAudioRecordingUrl() {
        return audioRecordingUrl;
    }

    public void setAudioRecordingUrl(String audioRecordingUrl) {
        this.audioRecordingUrl = audioRecordingUrl;
    }

    @DynamoDbSecondarySortKey(indexNames = "completed-calls-index")
    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

}