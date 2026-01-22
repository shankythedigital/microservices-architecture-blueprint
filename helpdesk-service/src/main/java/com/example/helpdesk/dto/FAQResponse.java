package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FAQResponse {
    private Long id;
    private String question;
    private String answer;
    private RelatedService relatedService;
    private String category;
    private Integer viewCount;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

