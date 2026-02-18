-- Fix inapp_log.message column to support 4-byte UTF-8 (emojis like 📦)
-- SQL Error 1366 occurs when 3-byte utf8 charset cannot store emoji characters
-- Note: ALTER DATABASE skipped - may not have privileges on managed DB (e.g. RDS)

-- Explicitly convert inapp_log table and message column to utf8mb4
ALTER TABLE inapp_log CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Ensure message column explicitly uses utf8mb4 (handles cases where table convert didn't apply)
ALTER TABLE inapp_log MODIFY COLUMN message TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
