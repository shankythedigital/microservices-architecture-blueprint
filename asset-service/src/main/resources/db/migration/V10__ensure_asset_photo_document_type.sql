-- Idempotent: ensure semantic type exists (V9 may be missing on some DBs or INSERT conflict).
INSERT IGNORE INTO document_type_master (code, description, active) VALUES
('asset_photo', 'User-uploaded appliance or asset photo', TRUE);
