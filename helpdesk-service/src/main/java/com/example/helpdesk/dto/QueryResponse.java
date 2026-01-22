package com.example.helpdesk.dto;

import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueryResponse {
    private Long id;
    private String question;
    private String answer;
    private QueryStatus status;
    private RelatedService relatedService;
    private String askedBy;
    private String answeredBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
}

