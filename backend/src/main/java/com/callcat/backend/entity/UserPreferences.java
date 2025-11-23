package com.callcat.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @Column(name = "timezone")
    private String timezone = "UTC";

    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Column(name = "voice_id")
    private String voiceId;

    @Column(name = "system_prompt", length = 5000)
    private String systemPrompt;

    public UserPreferences() {}

    public UserPreferences(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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