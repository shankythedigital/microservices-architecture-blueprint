package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * NotificationListResponse DTO for returning notification list
 * Used for displaying notifications in notification icons
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationListResponse {

    private Long id;
    private String userId;
    private String username;
    private String title;
    private String message;
    private String templateCode;
    private LocalDateTime createdAt;
    private Boolean read; // Can be added later for read/unread status
    private String priority; // Can be extracted from template or metadata

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

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

