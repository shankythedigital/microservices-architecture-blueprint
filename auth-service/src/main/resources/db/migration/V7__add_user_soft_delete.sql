-- Migration: Soft self-delete for users (user-initiated account deletion)
-- Adds deleted_at and deleted_by for audit trail. When set, user cannot log in.

ALTER TABLE users
ADD COLUMN deleted_at DATETIME NULL,
ADD COLUMN deleted_by VARCHAR(255) NULL;

CREATE INDEX idx_users_deleted_at ON users(deleted_at);
