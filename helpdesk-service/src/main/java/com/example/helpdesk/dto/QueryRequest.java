package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueryRequest {
    @NotBlank(message = "Question is required")
    private String question;

    @NotNull(message = "Related service is required")
    private RelatedService relatedService;
}

