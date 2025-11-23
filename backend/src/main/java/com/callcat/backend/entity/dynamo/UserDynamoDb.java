package com.callcat.backend.entity.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import com.callcat.backend.entity.Role;
import java.time.LocalDateTime;

@DynamoDbBean
public class UserDynamoDb {
    
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role = Role.USER;
    private Boolean isActive = true;
    private String passwordResetToken;
    private Long resetTokenExpires;
    private String createdAt;
    private String updatedAt;
    
    public UserDynamoDb() {}
    
    @DynamoDbPartitionKey
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey(indexNames = "byResetToken")
    public String getPasswordResetToken() {
        return passwordResetToken;
    }
    
    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }
    
    public Long getResetTokenExpires() {
        return resetTokenExpires;
    }
    
    public void setResetTokenExpires(Long resetTokenExpires) {
        this.resetTokenExpires = resetTokenExpires;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
