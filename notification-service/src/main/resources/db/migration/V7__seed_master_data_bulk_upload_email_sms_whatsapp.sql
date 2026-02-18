-- =======================================================================
-- Master Data Bulk Upload - One notification per channel: Email, SMS, WhatsApp only
-- Placeholders: entityType, totalCount, successCount, failureCount, notUploadedCount, username, timestamp
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MASTER_DATA_BULK_UPLOAD_EMAIL', 'Master Data Bulk Upload Summary', 'Bulk Upload Completed',
 'Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Not uploaded: {{notUploadedCount}}. By {{username}} at {{timestamp}}.',
 '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","notUploadedCount":"Not Uploaded","username":"User","timestamp":"Time"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, body, placeholders, project_type) VALUES
('MASTER_DATA_BULK_UPLOAD_SMS', 'Master Data Bulk Upload Summary',
 'Bulk upload {{entityType}}: Total {{totalCount}}, Success {{successCount}}, Failed {{failureCount}}, Not uploaded {{notUploadedCount}}. By {{username}}.',
 '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","notUploadedCount":"Not Uploaded","username":"User","timestamp":"Time"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MASTER_DATA_BULK_UPLOAD_WHATSAPP', 'Master Data Bulk Upload Summary', 'Bulk Upload Completed',
 'Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Not uploaded: {{notUploadedCount}}. By {{username}} at {{timestamp}}.',
 '{"entityType":"Entity Type","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","notUploadedCount":"Not Uploaded","username":"User","timestamp":"Time"}', 'ASSET_MGMT');
