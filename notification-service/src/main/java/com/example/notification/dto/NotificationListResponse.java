package com.example.notification.dto;

import com.example.common.util.PiiMaskingUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * NotificationListResponse DTO for returning notification list
 * Used for displaying notifications in notification icons
 * 🔐 DPDPA Compliance: All PII fields are automatically masked when serialized to JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationListResponse {

    private Long id;
    private String userId;      // Masked in getter
    private String username;    // Masked in getter
    private String title;
    private String message;
    private String templateCode;
    private LocalDateTime createdAt;
    private Boolean read; // Can be added later for read/unread status
    private String priority; // Can be extracted from template or metadata
    
    // Internal storage for unmasked values
    private transient String userIdUnmasked;
    private transient String usernameUnmasked;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /**
     * 🔐 DPDPA Compliance: Returns masked userId
     */
    public String getUserId() {
        if (userIdUnmasked != null) {
            return PiiMaskingUtil.maskUserId(userIdUnmasked);
        }
        return userId != null ? PiiMaskingUtil.maskUserId(userId) : null;
    }
    
    public void setUserId(String userId) {
        this.userIdUnmasked = userId;
        this.userId = userId;
    }

    /**
     * 🔐 DPDPA Compliance: Returns masked username
     */
    public String getUsername() {
        if (usernameUnmasked != null) {
            return PiiMaskingUtil.maskUsername(usernameUnmasked);
        }
        return username != null ? PiiMaskingUtil.maskUsername(username) : null;
    }
    
    public void setUsername(String username) {
        this.usernameUnmasked = username;
        this.username = username;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

