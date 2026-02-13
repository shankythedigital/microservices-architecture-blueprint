-- ============================================================
-- ✅ DPDPA Compliance: Encrypt PII Data Columns in Users Table
-- Migration to ensure encrypted columns for PII data
-- ============================================================

-- Users table: Ensure encrypted columns exist and are TEXT type
-- Note: These columns should already exist, but we ensure they're TEXT type for encryption
ALTER TABLE users 
MODIFY COLUMN IF EXISTS username_enc TEXT,
MODIFY COLUMN IF EXISTS email_enc TEXT,
MODIFY COLUMN IF EXISTS mobile_enc TEXT;

-- Note: UserDetailMaster table already has encrypted columns with proper setup
-- This migration ensures the users table columns are properly sized for encrypted data

