-- Numeric auth user id (auth DB user.user_id / JWT subject). Per issue row with id + optional asset_id.
ALTER TABLE issues
ADD COLUMN login_user_id BIGINT NULL;

CREATE INDEX idx_issues_login_user_id ON issues(login_user_id);
