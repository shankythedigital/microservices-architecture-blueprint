-- Migration: Add user profile fields to user_detail_master table
-- This migration adds fields for: photo, social media links, demographic info, behaviors, and additional info

-- Profile Photo
ALTER TABLE user_detail_master
ADD COLUMN profile_photo_url VARCHAR(500) NULL;

-- Social Media Links
ALTER TABLE user_detail_master
ADD COLUMN linkedin_url VARCHAR(500) NULL,
ADD COLUMN facebook_url VARCHAR(500) NULL,
ADD COLUMN twitter_url VARCHAR(500) NULL,
ADD COLUMN instagram_url VARCHAR(500) NULL,
ADD COLUMN github_url VARCHAR(500) NULL,
ADD COLUMN website_url VARCHAR(500) NULL;

-- Demographic Information (encrypted fields - using TEXT to avoid row size issues)
ALTER TABLE user_detail_master
ADD COLUMN date_of_birth_enc TEXT NULL,
ADD COLUMN gender_enc TEXT NULL,
ADD COLUMN occupation_enc TEXT NULL,
ADD COLUMN education_enc TEXT NULL,
ADD COLUMN marital_status_enc TEXT NULL;

-- Behaviors & Preferences (JSON strings stored as TEXT)
ALTER TABLE user_detail_master
ADD COLUMN preferences TEXT NULL,
ADD COLUMN activity_patterns TEXT NULL,
ADD COLUMN interests TEXT NULL;

-- Additional Information
ALTER TABLE user_detail_master
ADD COLUMN bio TEXT NULL,
ADD COLUMN skills TEXT NULL,
ADD COLUMN languages TEXT NULL,
ADD COLUMN timezone VARCHAR(100) NULL,
ADD COLUMN additional_info TEXT NULL;

-- Create indexes for commonly queried fields (optional, for performance)
CREATE INDEX idx_user_detail_profile_photo ON user_detail_master(profile_photo_url);
CREATE INDEX idx_user_detail_timezone ON user_detail_master(timezone);

