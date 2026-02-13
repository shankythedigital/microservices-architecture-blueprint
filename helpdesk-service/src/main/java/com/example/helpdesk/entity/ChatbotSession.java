package com.example.helpdesk.entity;

import com.example.common.converter.JpaAttributeEncryptor;
import com.example.common.jpa.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 🔐 DPDPA Compliance: All PII data (userId) is encrypted at rest.
 */
@Entity
@Table(name = "chatbot_sessions")
public class ChatbotSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔐 DPDPA Compliance: Encrypted PII data (may contain email addresses)
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "user_id_enc", nullable = false, columnDefinition = "TEXT")
    private String userId; // User ID or email

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatbotMessage> messages = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public List<ChatbotMessage> getMessages() { return messages; }
    public void setMessages(List<ChatbotMessage> messages) { this.messages = messages; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

