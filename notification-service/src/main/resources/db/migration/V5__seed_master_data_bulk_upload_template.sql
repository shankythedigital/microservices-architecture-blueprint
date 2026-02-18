-- =======================================================================
-- Master Data Bulk Upload - Single notification when bulk option is used
-- Placeholders: entityType, totalCount, successCount, failureCount, notUploadedCount, username, timestamp
-- =======================================================================
INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MASTER_DATA_BULK_UPLOAD_INAPP', 'Master Data Bulk Upload Summary', 'Bulk Upload Completed',
 '📦 Bulk upload for {{entityType}} completed. Total: {{totalCount}} | Success: {{successCount}} | Failed: {{failureCount}} | Not uploaded: {{notUploadedCount}}. By {{username}} at {{timestamp}}.',
 '{"entityType":"Entity Type (e.g. Product Category)","totalCount":"Total Rows","successCount":"Successful","failureCount":"Failed","notUploadedCount":"Not Uploaded","username":"User","timestamp":"Time"}', 'ASSET_MGMT');
