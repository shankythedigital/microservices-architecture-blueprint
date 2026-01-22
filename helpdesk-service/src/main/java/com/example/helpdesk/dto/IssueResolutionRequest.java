package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
public class IssueResolutionRequest {
    @NotBlank(message = "Resolution is required")
    private String resolution;

    // Getters and Setters
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}

