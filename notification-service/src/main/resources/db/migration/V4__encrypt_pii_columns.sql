-- ============================================================
-- ✅ DPDPA Compliance: Encrypt PII Data Columns
-- Migration to add encrypted columns for PII data
-- ============================================================

-- Notification Log: Add encrypted columns
ALTER TABLE notification_log 
ADD COLUMN IF NOT EXISTS username_enc TEXT,
ADD COLUMN IF NOT EXISTS email_enc TEXT;

-- SMS Log: Add encrypted columns
ALTER TABLE sms_log 
ADD COLUMN IF NOT EXISTS username_enc TEXT,
ADD COLUMN IF NOT EXISTS mobile_enc TEXT;

-- WhatsApp Log: Add encrypted columns
ALTER TABLE whatsapp_log 
ADD COLUMN IF NOT EXISTS username_enc TEXT,
ADD COLUMN IF NOT EXISTS mobile_enc TEXT;

-- In-App Log: Add encrypted username column
ALTER TABLE inapp_log 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- Audit Log: Add encrypted columns
ALTER TABLE audit_log 
ADD COLUMN IF NOT EXISTS username_enc TEXT,
ADD COLUMN IF NOT EXISTS user_id_enc TEXT;

-- Note: Existing data migration should be done separately
-- Old columns (username, email, mobile) can be dropped after data migration
-- For production, consider a phased approach:
-- 1. Add new encrypted columns
-- 2. Migrate existing data (encrypt and copy)
-- 3. Update application code
-- 4. Drop old columns after verification

