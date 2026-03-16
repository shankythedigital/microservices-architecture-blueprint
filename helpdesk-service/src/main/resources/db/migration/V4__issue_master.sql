-- Issue Master: predefined issue types linked to category, subcategory, component, or spare part
-- When issue is not in list, user can add by selecting from master tables (asset-service)
CREATE TABLE IF NOT EXISTS issue_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_title VARCHAR(255) NOT NULL,
    issue_description TEXT,
    category_id BIGINT NULL,
    sub_category_id BIGINT NULL,
    component_id BIGINT NULL,
    spare_part_id BIGINT NULL,
    -- BaseEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_issue_master_category ON issue_master(category_id);
CREATE INDEX idx_issue_master_sub_category ON issue_master(sub_category_id);
CREATE INDEX idx_issue_master_component ON issue_master(component_id);
CREATE INDEX idx_issue_master_spare_part ON issue_master(spare_part_id);
