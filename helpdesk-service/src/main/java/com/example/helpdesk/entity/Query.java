package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "queries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    @Column(nullable = false)
    private String askedBy; // User ID or email

    private String answeredBy; // Support agent ID or email

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PreUpdate
    protected void onUpdate() {
        if (status == QueryStatus.ANSWERED && answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }
}

