-- Dedicated doc type for user-uploaded appliance photos (coexists with invoice doc on same ASSET).
INSERT INTO document_type_master (code, description, active) VALUES
('asset_photo', 'User-uploaded appliance or asset photo', TRUE);
