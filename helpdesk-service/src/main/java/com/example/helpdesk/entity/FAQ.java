package com.example.helpdesk.entity;

import com.example.common.jpa.BaseEntity;
import com.example.helpdesk.enums.RelatedService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
}

