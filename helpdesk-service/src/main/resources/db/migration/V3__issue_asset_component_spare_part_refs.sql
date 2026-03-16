-- Add optional references to asset, component, and spare parts master
-- Allows linking issues to specific assets, components, or spare parts
ALTER TABLE issues
ADD COLUMN asset_id BIGINT NULL,
ADD COLUMN component_id BIGINT NULL,
ADD COLUMN spare_part_id BIGINT NULL;

CREATE INDEX idx_issues_asset_id ON issues(asset_id);
CREATE INDEX idx_issues_component_id ON issues(component_id);
CREATE INDEX idx_issues_spare_part_id ON issues(spare_part_id);
