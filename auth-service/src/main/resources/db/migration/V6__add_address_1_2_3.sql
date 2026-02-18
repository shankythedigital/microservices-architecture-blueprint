-- Migration: Add address1, address2, address3 to user_detail_master table
-- These fields store additional address lines (admin only can update via profile)

ALTER TABLE user_detail_master
ADD COLUMN address1_enc TEXT NULL,
ADD COLUMN address2_enc TEXT NULL,
ADD COLUMN address3_enc TEXT NULL;
