-- Add Address 1, Address 2, Address 3 columns to user_detail_master
-- Encrypted for PII consistency (same pattern as pincode, city, state, country)
ALTER TABLE user_detail_master
ADD COLUMN address1_enc TEXT NULL,
ADD COLUMN address2_enc TEXT NULL,
ADD COLUMN address3_enc TEXT NULL;
