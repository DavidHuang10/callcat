package com.callcat.backend.entity.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class UserPreferencesDynamoDb {
    
    private String email;
    private String timezone = "UTC";
    private Boolean emailNotifications = true;
    private String voiceId;
    private String systemPrompt;
    
    public UserPreferencesDynamoDb() {}
    
    @DynamoDbPartitionKey
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
