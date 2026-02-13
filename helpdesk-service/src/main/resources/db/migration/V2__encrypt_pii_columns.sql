-- ============================================================
-- ✅ DPDPA Compliance: Encrypt PII Data Columns
-- Migration to add encrypted columns for PII data
-- ============================================================

-- Issues: Add encrypted columns
ALTER TABLE issues 
ADD COLUMN IF NOT EXISTS reported_by_enc TEXT,
ADD COLUMN IF NOT EXISTS assigned_to_enc TEXT;

-- Queries: Add encrypted columns
ALTER TABLE queries 
ADD COLUMN IF NOT EXISTS asked_by_enc TEXT,
ADD COLUMN IF NOT EXISTS answered_by_enc TEXT;

-- Chatbot Sessions: Add encrypted user_id column
ALTER TABLE chatbot_sessions 
ADD COLUMN IF NOT EXISTS user_id_enc TEXT;

-- Issue Escalations: Add encrypted escalated_by column
ALTER TABLE issue_escalations 
ADD COLUMN IF NOT EXISTS escalated_by_enc TEXT;

-- Note: Existing data migration should be done separately
-- Old columns (reported_by, assigned_to, asked_by, answered_by, user_id, escalated_by) 
-- can be dropped after data migration
-- For production, consider a phased approach:
-- 1. Add new encrypted columns
-- 2. Migrate existing data (encrypt and copy)
-- 3. Update application code
-- 4. Drop old columns after verification

