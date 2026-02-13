package com.example.helpdesk.entity;

import com.example.common.converter.JpaAttributeEncryptor;
import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 🔐 DPDPA Compliance: All PII data (askedBy, answeredBy) is encrypted at rest.
 */
@Entity
@Table(name = "queries")
public class Query extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryStatus status = QueryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedService relatedService;

    // 🔐 DPDPA Compliance: Encrypted PII data (may contain email addresses)
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "asked_by_enc", nullable = false, columnDefinition = "TEXT")
    private String askedBy; // User ID or email

    // 🔐 DPDPA Compliance: Encrypted PII data (may contain email addresses)
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "answered_by_enc", columnDefinition = "TEXT")
    private String answeredBy; // Support agent ID or email

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PreUpdate
    protected void onUpdate() {
        if (status == QueryStatus.ANSWERED && answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public QueryStatus getStatus() { return status; }
    public void setStatus(QueryStatus status) { this.status = status; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public String getAskedBy() { return askedBy; }
    public void setAskedBy(String askedBy) { this.askedBy = askedBy; }
    public String getAnsweredBy() { return answeredBy; }
    public void setAnsweredBy(String answeredBy) { this.answeredBy = answeredBy; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}

