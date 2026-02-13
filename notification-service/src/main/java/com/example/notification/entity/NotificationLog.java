package com.example.notification.entity;

import com.example.common.converter.JpaAttributeEncryptor;
import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;

/**
 * 🔐 DPDPA Compliance: All PII data (username, email) is encrypted at rest.
 */
@Entity
@Table(name = "notification_log")
public class NotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔐 DPDPA Compliance: Encrypted PII data
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", columnDefinition = "TEXT")
    private String username;

    // 🔐 DPDPA Compliance: Encrypted PII data
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "email_enc", columnDefinition = "TEXT")
    private String email;

    private String emailFingerprint;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String channel;
    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEmailFingerprint() { return emailFingerprint; }
    public void setEmailFingerprint(String emailFingerprint) { this.emailFingerprint = emailFingerprint; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
