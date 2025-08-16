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
    private String callerNumber;
    private String subject;
    private String prompt;
    private String status;
    private Long scheduledFor;
    private Long callStartedAt;
    
    private String aiLanguage;
    private String voiceId;
    private Long createdAt;
    private Long updatedAt;
    
    // During-call fields
    private String providerId;
    
    // Post-call fields
    private Long completedAt;
    
    // Retell-specific data storage (as JSON string for DynamoDB compatibility)
    private String retellCallData;

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

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
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
    public Long getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Long scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public Long getCallStartedAt() {
        return callStartedAt;
    }

    public void setCallStartedAt(Long callStartedAt) {
        this.callStartedAt = callStartedAt;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
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


    @DynamoDbSecondarySortKey(indexNames = "completed-calls-index")
    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public String getRetellCallData() {
        return retellCallData;
    }

    public void setRetellCallData(String retellCallData) {
        this.retellCallData = retellCallData;
    }

}