package com.example.helpdesk.dto;

import com.example.common.util.PiiMaskingUtil;
import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import java.time.LocalDateTime;

/**
 * 🔐 DPDPA Compliance: All PII fields are automatically masked when serialized to JSON.
 */
public class QueryResponse {
    private Long id;
    private String question;
    private String answer;
    private QueryStatus status;
    private RelatedService relatedService;
    private String askedBy;      // Masked in getter
    private String answeredBy;   // Masked in getter
    
    // Internal storage for unmasked values
    private transient String askedByUnmasked;
    private transient String answeredByUnmasked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
    private Long loginUserId;

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
    
    /**
     * 🔐 DPDPA Compliance: Returns masked askedBy
     */
    public String getAskedBy() {
        if (askedByUnmasked != null) {
            return PiiMaskingUtil.maskUserId(askedByUnmasked);
        }
        return askedBy != null ? PiiMaskingUtil.maskUserId(askedBy) : null;
    }
    
    public void setAskedBy(String askedBy) {
        this.askedByUnmasked = askedBy;
        this.askedBy = askedBy;
    }
    
    /**
     * 🔐 DPDPA Compliance: Returns masked answeredBy
     */
    public String getAnsweredBy() {
        if (answeredByUnmasked != null) {
            return PiiMaskingUtil.maskUserId(answeredByUnmasked);
        }
        return answeredBy != null ? PiiMaskingUtil.maskUserId(answeredBy) : null;
    }
    
    public void setAnsweredBy(String answeredBy) {
        this.answeredByUnmasked = answeredBy;
        this.answeredBy = answeredBy;
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    public Long getLoginUserId() { return loginUserId; }
    public void setLoginUserId(Long loginUserId) { this.loginUserId = loginUserId; }
}

