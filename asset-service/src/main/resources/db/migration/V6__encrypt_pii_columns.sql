-- ============================================================
-- ✅ DPDPA Compliance: Encrypt PII Data Columns
-- Migration to add encrypted columns for PII data
-- ============================================================

-- Asset User Link: Add encrypted columns
ALTER TABLE asset_user_link 
ADD COLUMN IF NOT EXISTS username_enc TEXT,
ADD COLUMN IF NOT EXISTS email_enc TEXT,
ADD COLUMN IF NOT EXISTS mobile_enc TEXT;

-- Asset Purchase Info: Add encrypted username column
ALTER TABLE asset_purchase_info 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- OCR Training Data: Add encrypted username column
ALTER TABLE ocr_training_data 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- Asset Warranty: Add encrypted username column
ALTER TABLE asset_warranty 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- Asset AMC: Add encrypted username column
ALTER TABLE asset_amc 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- Asset Document: Add encrypted username column
ALTER TABLE asset_document 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- Audit Log: Add encrypted username column
ALTER TABLE audit_log 
ADD COLUMN IF NOT EXISTS username_enc TEXT;

-- Note: Existing data migration should be done separately
-- Old columns (username, email, mobile) can be dropped after data migration
-- For production, consider a phased approach:
-- 1. Add new encrypted columns
-- 2. Migrate existing data (encrypt and copy)
-- 3. Update application code
-- 4. Drop old columns after verification

