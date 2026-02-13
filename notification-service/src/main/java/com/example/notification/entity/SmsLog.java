package com.example.notification.entity;

import com.example.common.converter.JpaAttributeEncryptor;
import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;

/**
 * 🔐 DPDPA Compliance: All PII data (username, mobile) is encrypted at rest.
 */
@Entity
@Table(name = "sms_log")
public class SmsLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔐 DPDPA Compliance: Encrypted PII data
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", columnDefinition = "TEXT")
    private String username;

    // 🔐 DPDPA Compliance: Encrypted PII data
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "mobile_enc", columnDefinition = "TEXT")
    private String mobile;

    private String mobileFingerprint;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String templateCode;
    private String providerMessageId;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    private Integer retries = 0;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getMobileFingerprint() { return mobileFingerprint; }
    public void setMobileFingerprint(String mobileFingerprint) { this.mobileFingerprint = mobileFingerprint; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public Integer getRetries() { return retries; }
    public void setRetries(Integer retries) { this.retries = retries; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
