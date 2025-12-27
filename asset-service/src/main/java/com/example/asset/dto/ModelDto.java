
package com.example.asset.dto;

import com.example.common.jpa.BaseEntity;

/**
 * ✅ ModelDto
 * Safe DTO for API responses — avoids exposing JPA entities directly.
 */
public class ModelDto extends BaseEntity {

    private Long modelId;
    private String modelName;
    private String description;
    private Boolean active;

    private Long makeId;
    private String makeName;

    
    // ----- Getters & Setters -----
    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }
}


