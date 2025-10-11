package com.example.notification.dto;

import jakarta.persistence.*;

import java.util.Map;

/**
 * DTO for accepting notification requests across multiple channels.
 * Supports SMS, WhatsApp, Email/Notification, and In-App notifications.
 */
public class NotificationRequest {

    /** Target channel: SMS, WHATSAPP, EMAIL, NOTIFICATION, INAPP */
    private String channel;

    /** Username of the recipient (optional but recommended for personalization) */
    private String username;

    /** Mobile number (used for SMS/WhatsApp) */
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    /** Email address (used for Email/Notification) */
    @Column(name = "email_enc", length = 2048)
    private String email;

    /** Subject or title (used for Email/InApp) */
    private String subject;

    /** Code of the template being used (e.g., OTP, ORDER_CONFIRM) */
    private String templateCode;

    /** Map of placeholder keys -> values for rendering templates */
    private Map<String, Object> placeholders;

    /** ID of the user who triggered the notification (for audit/logging) */
    private String userId;

    // ----------------- Getters & Setters -----------------

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, Object> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(Map<String, Object> placeholders) {
        this.placeholders = placeholders;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

