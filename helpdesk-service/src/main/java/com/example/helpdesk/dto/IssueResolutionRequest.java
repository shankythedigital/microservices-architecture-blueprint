package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IssueResolutionRequest {
    @NotBlank(message = "Resolution is required")
    private String resolution;
}

