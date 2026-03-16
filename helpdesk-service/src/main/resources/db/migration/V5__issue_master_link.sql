-- Link issues (tickets) to issue master when raised from issue list
ALTER TABLE issues ADD COLUMN issue_master_id BIGINT NULL;
ALTER TABLE issues ADD COLUMN category_id BIGINT NULL;
ALTER TABLE issues ADD COLUMN sub_category_id BIGINT NULL;

CREATE INDEX idx_issues_issue_master_id ON issues(issue_master_id);
CREATE INDEX idx_issues_category_id ON issues(category_id);
CREATE INDEX idx_issues_sub_category_id ON issues(sub_category_id);
