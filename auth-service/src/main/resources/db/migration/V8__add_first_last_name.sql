-- Migration: Add first name and last name to user profile (PII - encrypted)
ALTER TABLE user_detail_master
ADD COLUMN first_name_enc TEXT NULL,
ADD COLUMN last_name_enc TEXT NULL;
