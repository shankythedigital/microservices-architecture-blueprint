package com.example.asset.dto;

import java.util.List;

public class BulkMakeRequest {

    private Long userId;
    private String username;
    private String projectType;

    private List<SimpleMakeDto> makes;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public List<SimpleMakeDto> getMakes() { return makes; }
    public void setMakes(List<SimpleMakeDto> makes) { this.makes = makes; }

    public static class SimpleMakeDto {
        private Long makeId; // Primary key from Excel
        private String makeName;
        private String subCategoryName; // For lookup by name
        private Long subCategoryId; // Foreign key from Excel (sub_category_id)

        public Long getMakeId() { return makeId; }
        public void setMakeId(Long makeId) { this.makeId = makeId; }

        public String getMakeName() { return makeName; }
        public void setMakeName(String makeName) { this.makeName = makeName; }

        public String getSubCategoryName() { return subCategoryName; }
        public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }

        public Long getSubCategoryId() { return subCategoryId; }
        public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }
    }
}

