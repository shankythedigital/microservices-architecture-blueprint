package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.RelatedService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_knowledge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceKnowledge extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelatedService service;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String category;

    @Column(name = "api_endpoints", columnDefinition = "TEXT")
    private String apiEndpoints; // JSON or comma-separated list

    @Column(name = "common_issues", columnDefinition = "TEXT")
    private String commonIssues; // JSON or text

    @Column(name = "troubleshooting_steps", columnDefinition = "TEXT")
    private String troubleshootingSteps;
}

