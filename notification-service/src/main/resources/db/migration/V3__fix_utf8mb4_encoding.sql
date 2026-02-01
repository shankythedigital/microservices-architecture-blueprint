-- Fix UTF8MB4 encoding for emoji support in MySQL 11.4.4
-- This migration ensures all text columns support 4-byte UTF-8 characters (emojis)

-- Fix inapp_template_master table
ALTER TABLE inapp_template_master 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Fix notification_template_master table (for consistency)
ALTER TABLE notification_template_master 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Fix sms_template_master table (for consistency)
ALTER TABLE sms_template_master 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Fix whatsapp_template_master table (for consistency)
ALTER TABLE whatsapp_template_master 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Fix log tables that might also contain emojis
ALTER TABLE sms_log 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE notification_log 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE whatsapp_log 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE inapp_log 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE audit_log 
  CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

