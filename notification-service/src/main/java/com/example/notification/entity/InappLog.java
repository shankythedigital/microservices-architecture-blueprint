package com.example.notification.entity;

// import com.example.notification.crypto.JpaAttributeEncryptor;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "inapp_log")
public class InappLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Convert(converter = com.example.notification.crypto.JpaAttributeEncryptor.class)
    private String username;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String templateCode;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
