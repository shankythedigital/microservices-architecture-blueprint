package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.RelatedService;
import jakarta.persistence.*;
@Entity
@Table(name = "faqs")
public class FAQ extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedService relatedService;

    @Column(nullable = false)
    private String category;

    private Integer viewCount = 0;

    private Integer helpfulCount = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }
}

