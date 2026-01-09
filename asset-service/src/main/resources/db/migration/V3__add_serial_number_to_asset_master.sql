-- ============================================================
-- Migration: Add serial_number column to asset_master table
-- ============================================================
-- This migration adds the serial_number field to support
-- complete asset creation with serial number tracking.
-- ============================================================

-- Add serial_number column to asset_master table
ALTER TABLE asset_master 
ADD COLUMN IF NOT EXISTS serial_number VARCHAR(255) NULL 
AFTER asset_name_udv;

-- Add index for faster lookups by serial number (optional but recommended)
CREATE INDEX IF NOT EXISTS idx_asset_master_serial_number 
ON asset_master(serial_number);

-- Note: purchase_date already exists in the table from V1__init.sql
-- No migration needed for purchase_date field

