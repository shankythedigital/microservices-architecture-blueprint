
package com.example.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ✅ Notification Retry Log Entity
 * Stores failed notification attempts for later reprocessing.
 * Used by NotificationRetryProcessor to periodically retry sending.
 */
@Entity
@Table(name = "notification_retry_log")
public class NotificationRetryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private String channel;
    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String payloadJson; // serialized placeholders/payload

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private int retryCount;
    private boolean processed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastAttemptAt = LocalDateTime.now();

    // ------------------------------------------------------------------------
    // ✅ Constructors
    // ------------------------------------------------------------------------

    public NotificationRetryLog() {}

    public NotificationRetryLog(Long userId,
                                String username,
                                String channel,
                                String templateCode,
                                String payloadJson,
                                String errorMessage,
                                int retryCount) {
        this.userId = userId;
        this.username = username;
        this.channel = channel;
        this.templateCode = templateCode;
        this.payloadJson = payloadJson;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.createdAt = LocalDateTime.now();
        this.lastAttemptAt = LocalDateTime.now();
        this.processed = false;
    }

    // ✅ Getters and setters omitted for brevity
    // (Include all fields with Lombok @Data if you use Lombok)

    // ------------------------------------------------------------------------
    // 🧩 Getters and Setters (if not using Lombok)
    // ------------------------------------------------------------------------

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getChannel() { return channel; }

    public void setChannel(String channel) { this.channel = channel; }

    public String getTemplateCode() { return templateCode; }

    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getPayloadJson() { return payloadJson; }

    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getErrorMessage() { return errorMessage; }

    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getRetryCount() { return retryCount; }

    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public boolean isProcessed() { return processed; }

    public void setProcessed(boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
}


