package com.example.asset.dto;

import java.util.List;

public class BulkComponentRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleComponentDto> components;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleComponentDto> getComponents() { return components; }
    public void setComponents(List<SimpleComponentDto> components) { this.components = components; }

    public static class SimpleComponentDto {
        private Long componentId; // Primary key from Excel
        private String componentName;
        private String description;

        public Long getComponentId() { return componentId; }
        public void setComponentId(Long componentId) { this.componentId = componentId; }

        public String getComponentName() { return componentName; }
        public void setComponentName(String componentName) { this.componentName = componentName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

