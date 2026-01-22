package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FAQRequest {
    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;

    @NotNull(message = "Related service is required")
    private RelatedService relatedService;

    @NotBlank(message = "Category is required")
    private String category;
}

