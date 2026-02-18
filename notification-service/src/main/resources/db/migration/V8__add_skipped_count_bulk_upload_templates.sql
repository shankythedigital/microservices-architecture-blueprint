-- =======================================================================
-- Update Master Data Bulk Upload templates to include skippedCount for single-line summary
-- Placeholders: entityType, totalCount, successCount, failureCount, skippedCount, username, timestamp
-- =======================================================================
UPDATE notification_template_master SET body = 'Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Skipped: {{skippedCount}}. By {{username}} at {{timestamp}}.',
 placeholders = '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","skippedCount":"Skipped","username":"User","timestamp":"Time"}'
 WHERE template_code = 'MASTER_DATA_BULK_UPLOAD_EMAIL' AND project_type = 'ASSET_MGMT';

UPDATE sms_template_master SET body = 'Bulk upload {{entityType}}: Total {{totalCount}}, Success {{successCount}}, Failed {{failureCount}}, Skipped {{skippedCount}}. By {{username}}.',
 placeholders = '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","skippedCount":"Skipped","username":"User","timestamp":"Time"}'
 WHERE template_code = 'MASTER_DATA_BULK_UPLOAD_SMS' AND project_type = 'ASSET_MGMT';

UPDATE whatsapp_template_master SET body = 'Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Skipped: {{skippedCount}}. By {{username}} at {{timestamp}}.',
 placeholders = '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","skippedCount":"Skipped","username":"User","timestamp":"Time"}'
 WHERE template_code = 'MASTER_DATA_BULK_UPLOAD_WHATSAPP' AND project_type = 'ASSET_MGMT';

-- InApp template (from V5)
UPDATE inapp_template_master SET body = 'Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Skipped: {{skippedCount}}. By {{username}} at {{timestamp}}.',
 placeholders = '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","skippedCount":"Skipped","username":"User","timestamp":"Time"}'
 WHERE template_code = 'MASTER_DATA_BULK_UPLOAD_INAPP' AND project_type = 'ASSET_MGMT';
