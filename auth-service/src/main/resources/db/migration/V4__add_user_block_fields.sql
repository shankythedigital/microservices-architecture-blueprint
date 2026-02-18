-- Migration: Block / Unblock / Permanent block (Security, Compliance, PDPA/DPDPA)
-- Adds block_type, reason, and audit fields for access control and lawful basis.

ALTER TABLE user_detail_master
ADD COLUMN block_type VARCHAR(20) DEFAULT 'NONE',
ADD COLUMN block_reason VARCHAR(500) NULL,
ADD COLUMN blocked_at DATETIME NULL,
ADD COLUMN blocked_by VARCHAR(255) NULL,
ADD COLUMN blocked_until DATETIME NULL;

-- NONE = active, TEMPORARY = blocked until unblock/blocked_until, PERMANENT = permanent block
UPDATE user_detail_master SET block_type = 'NONE' WHERE block_type IS NULL;

CREATE INDEX idx_user_detail_block_type ON user_detail_master(block_type);
CREATE INDEX idx_user_detail_blocked_until ON user_detail_master(blocked_until);
