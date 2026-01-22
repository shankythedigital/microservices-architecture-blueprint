package com.example.helpdesk.dto;

import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private IssuePriority priority;

    @NotNull(message = "Related service is required")
    private RelatedService relatedService;
}

