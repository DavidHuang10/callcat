package com.callcat.backend.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class CallRecord {
    private String userId;
    private String sk; // Composite sort key: scheduledForMs#callId
    private String callId;
    private String calleeName;
    private String phoneNumber;
    private String callerNumber;
    private String subject;
    private String prompt;
    private String status;
    private Long scheduledFor;

    private String aiLanguage;
    private String voiceId;
    private Long createdAt;
    private Long updatedAt;
    
    // During-call fields
    private String providerId;
    
    // Post-call fields
    private Long completedAt;
    private Boolean isSuccessful;
    
    // Retell-specific data storage (as JSON string for DynamoDB compatibility)
    private String retellCallData;

    public CallRecord() {}

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbSortKey
    public String getSk() {
        if (sk == null && scheduledFor != null && callId != null) {
            return String.format("%013d#%s", scheduledFor, callId);
        }
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "byCallId")
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
        // Validate status
        if (status != null && !"SCHEDULED".equals(status) && !"COMPLETED".equals(status)) {
            throw new IllegalArgumentException("Status must be SCHEDULED or COMPLETED, got: " + status);
        }
        this.status = status;
    }

    public Long getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Long scheduledFor) {
        this.scheduledFor = scheduledFor;
        // Recompute SK when scheduledFor changes
        this.sk = null;
    }


    @DynamoDbSecondaryPartitionKey(indexNames = "byProvider")
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @DynamoDbSecondarySortKey(indexNames = "byProvider")
    public String getProviderSk() {
        return getSk(); // Use same SK for provider index
    }

    public void setProviderSk(String providerSk) {
        // No-op setter for DynamoDB Enhanced Client
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

    // Single elegant GSI partition key for status-based queries
    @DynamoDbSecondaryPartitionKey(indexNames = "byUserStatus")
    public String getUserStatus() {
        if (userId != null && status != null) {
            return userId + "#" + status;
        }
        return null;
    }

    public void setUserStatus(String userStatus) {
        // No-op setter required by DynamoDB Enhanced Client
    }

    @DynamoDbSecondarySortKey(indexNames = "byUserStatus")
    public String getUserStatusSk() {
        return getSk(); // Use same SK for user status index
    }

    public void setUserStatusSk(String userStatusSk) {
        // No-op setter for DynamoDB Enhanced Client
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(Boolean successful) {
        isSuccessful = successful;
    }
}