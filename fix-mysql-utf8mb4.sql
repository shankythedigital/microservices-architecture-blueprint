-- Quick fix for MySQL 11.4.4 emoji support
-- Run this directly in MySQL to fix the inapp_template_master table

USE notificationdb;

-- Fix the inapp_template_master table to support emojis
ALTER TABLE inapp_template_master 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify the change
SHOW CREATE TABLE inapp_template_master;

-- Now you can insert the emoji data
INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('FILE_DOWNLOAD_INAPP', 'File Download', 'File Downloaded', '📥 File {{fileName}} downloaded successfully by {{username}}.', '{"fileName":"File Name","username":"Downloaded By"}', 'ASSET_MGMT');

