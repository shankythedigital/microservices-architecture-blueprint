-- Ensure username/email/mobile are unique at the database level (MySQL).
-- We use IF checks via a temporary procedure so this migration is re-runnable
-- across environments where indexes may already exist.

DROP PROCEDURE IF EXISTS ensure_unique_user_identifiers;
DELIMITER $$
CREATE PROCEDURE ensure_unique_user_identifiers()
BEGIN
  DECLARE idx_count INT DEFAULT 0;

  -- users: uniqueness per project_type
  SELECT COUNT(1) INTO idx_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'users'
    AND index_name = 'uk_users_username_hash_project_type';
  IF idx_count = 0 THEN
    SET @sql = 'CREATE UNIQUE INDEX uk_users_username_hash_project_type ON users (username_hash, project_type)';
    PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;

  SELECT COUNT(1) INTO idx_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'users'
    AND index_name = 'uk_users_email_hash_project_type';
  IF idx_count = 0 THEN
    SET @sql = 'CREATE UNIQUE INDEX uk_users_email_hash_project_type ON users (email_hash, project_type)';
    PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;

  SELECT COUNT(1) INTO idx_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'users'
    AND index_name = 'uk_users_mobile_hash_project_type';
  IF idx_count = 0 THEN
    SET @sql = 'CREATE UNIQUE INDEX uk_users_mobile_hash_project_type ON users (mobile_hash, project_type)';
    PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;

  -- user_detail_master: global uniqueness (across project types)
  SELECT COUNT(1) INTO idx_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user_detail_master'
    AND index_name = 'uk_udm_username_hash';
  IF idx_count = 0 THEN
    SET @sql = 'CREATE UNIQUE INDEX uk_udm_username_hash ON user_detail_master (username_hash)';
    PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;

  SELECT COUNT(1) INTO idx_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user_detail_master'
    AND index_name = 'uk_udm_email_hash';
  IF idx_count = 0 THEN
    SET @sql = 'CREATE UNIQUE INDEX uk_udm_email_hash ON user_detail_master (email_hash)';
    PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;

  SELECT COUNT(1) INTO idx_count
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user_detail_master'
    AND index_name = 'uk_udm_mobile_hash';
  IF idx_count = 0 THEN
    SET @sql = 'CREATE UNIQUE INDEX uk_udm_mobile_hash ON user_detail_master (mobile_hash)';
    PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL ensure_unique_user_identifiers();
DROP PROCEDURE IF EXISTS ensure_unique_user_identifiers;

