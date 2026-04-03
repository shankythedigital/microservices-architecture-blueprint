-- Filter "my queries" by numeric auth user id (JWT subject). Encrypted asked_by_enc is not equality-searchable with random IV.
ALTER TABLE queries
ADD COLUMN login_user_id BIGINT NULL;

CREATE INDEX idx_queries_login_user_id ON queries(login_user_id);
