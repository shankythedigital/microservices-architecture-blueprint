-- V7__document_type_master.sql
-- Document type master table with BaseEntity audit fields.
-- Seeds data for allowed document types (pdf, doc, docx, jpg, etc.).

-- ============================
-- DOCUMENT TYPE MASTER
-- ============================
CREATE TABLE IF NOT EXISTS document_type_master (
  document_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- ============================
-- SEED DATA
-- ============================
INSERT INTO document_type_master (code, description, active) VALUES
('pdf', 'Portable Document Format', TRUE),
('doc', 'Microsoft Word 97-2003 Document', TRUE),
('docx', 'Microsoft Word Document', TRUE),
('txt', 'Plain Text File', TRUE),
('rtf', 'Rich Text Format', TRUE),
('odt', 'OpenDocument Text', TRUE),
('html', 'HyperText Markup Language', TRUE),
('htm', 'HyperText Markup (alternate)', TRUE),
('xml', 'Extensible Markup Language', TRUE),
('json', 'JavaScript Object Notation', TRUE),
('yaml', 'YAML Ain''t Markup Language', TRUE),
('yml', 'YAML Configuration', TRUE),
('csv', 'Comma-Separated Values', TRUE),
('xls', 'Microsoft Excel 97-2003', TRUE),
('xlsx', 'Microsoft Excel Workbook', TRUE),
('ods', 'OpenDocument Spreadsheet', TRUE),
('ppt', 'Microsoft PowerPoint 97-2003', TRUE),
('pptx', 'Microsoft PowerPoint Presentation', TRUE),
('odp', 'OpenDocument Presentation', TRUE),
('md', 'Markdown Document', TRUE),
('tex', 'LaTeX Document', TRUE),
('epub', 'Electronic Publication', TRUE),
('mobi', 'Mobipocket eBook', TRUE),
('log', 'Log File', TRUE),
('ini', 'Initialization/Config File', TRUE),
('cfg', 'Configuration File', TRUE),
('jpg', 'JPEG Image', TRUE),
('jpeg', 'JPEG Image (alternate)', TRUE),
('png', 'Portable Network Graphics', TRUE),
('gif', 'Graphics Interchange Format', TRUE),
('bmp', 'Bitmap Image', TRUE),
('tiff', 'Tagged Image File Format', TRUE),
('tif', 'Tagged Image File (alternate)', TRUE),
('webp', 'WebP Image', TRUE),
('svg', 'Scalable Vector Graphics', TRUE),
('heic', 'High Efficiency Image Container', TRUE),
('heif', 'High Efficiency Image Format', TRUE),
('raw', 'Raw Image Data', TRUE),
('ico', 'Icon File', TRUE),
('psd', 'Adobe Photoshop Document', TRUE)
;
