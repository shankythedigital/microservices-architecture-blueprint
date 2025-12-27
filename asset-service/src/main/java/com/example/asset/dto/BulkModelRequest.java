package com.example.asset.dto;

import java.util.List;

public class BulkModelRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleModelDto> models;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleModelDto> getModels() { return models; }
    public void setModels(List<SimpleModelDto> models) { this.models = models; }

    public static class SimpleModelDto {
        private Long modelId; // Primary key from Excel
        private String modelName;
        private String description;
        private String makeName; // For lookup by name
        private Long makeId; // Foreign key from Excel (make_id)

        public Long getModelId() { return modelId; }
        public void setModelId(Long modelId) { this.modelId = modelId; }

        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getMakeName() { return makeName; }
        public void setMakeName(String makeName) { this.makeName = makeName; }

        public Long getMakeId() { return makeId; }
        public void setMakeId(Long makeId) { this.makeId = makeId; }
    }
}

