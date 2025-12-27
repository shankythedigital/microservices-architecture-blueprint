
-- ========================================
-- Seed Asset Management Templates
-- ========================================
--  truncate table sms_template_master; truncate table whatsapp_template_master; truncate table notification_template_master; truncate table inapp_template_master; 

-- WhatsApp Templates
INSERT INTO whatsapp_template_master 
(template_code, name, subject, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_WA', 'Asset Assignment', 'Asset Assigned',
  '📌 Asset {{assetId}} has been assigned to you, {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_WA', 'Asset Return', 'Asset Returned',
  '↩️ Asset {{assetId}} returned successfully by {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_WA', 'Maintenance Alert', 'Asset Maintenance Scheduled',
  '⚙️ Asset {{assetId}} scheduled for maintenance on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_WA', 'Asset Error', 'Asset Error Notification',
  '⚠️ Asset {{assetId}} error {{errorCode}} at {{timestamp}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code","timestamp":"Error Time"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('WELCOME_WA', 'Welcome WhatsApp', 'Welcome',
  '👋 Hi {{name}}, welcome to Our Store!',
  '{"name":"Customer Name"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_WA', 'Order Confirmation', 'Order Confirmed',
  '✅ Order {{orderId}} confirmed for {{name}}.',
  '{"orderId":"Order ID","name":"Customer Name"}', TRUE, 'ECOM'),

 ('SHIPMENT_WA', 'Shipment Update', 'Order Shipped',
  '📦 Order {{orderId}} has been shipped. Track here: {{trackingLink}}',
  '{"orderId":"Order ID","trackingLink":"Tracking URL"}', TRUE, 'ECOM'),

 ('DELIVERY_WA', 'Delivery Notification', 'Order Delivered',
  '🎉 Order {{orderId}} delivered successfully.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('ALERT_WA', 'System Alert', 'System Alert',
  '⚠️ Alert: {{alertMessage}}',
  '{"alertMessage":"Alert Details"}', TRUE, 'ECOM'),


 ('OTP_WA', 'OTP Verification', 'OTP Verification',
  'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM');



-- SMS Templates
INSERT INTO sms_template_master 
(template_code, name, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_SMS', 'Asset Assignment', 'Asset {{assetId}} has been assigned to you.',
  '{"assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_SMS', 'Asset Return', 'Return logged for asset {{assetId}}.',
  '{"assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_SMS', 'Maintenance Alert', 'Maintenance scheduled for asset {{assetId}} on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_SMS', 'Asset Error', 'Asset {{assetId}} error: {{errorCode}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('OTP_SMS', 'OTP Verification', 'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_SMS', 'Order Confirmation', 'Your order {{orderId}} has been confirmed.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('SHIPMENT_SMS', 'Shipment Update', 'Your order {{orderId}} has been shipped.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('DELIVERY_SMS', 'Delivery Notification', 'Your order {{orderId}} has been delivered.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('ERROR_SMS', 'Error Alert', 'System error occurred: {{errorCode}}',
  '{"errorCode":"Error Code"}', TRUE, 'ECOM');

-- Notification / Email Templates
INSERT INTO notification_template_master 
(template_code, name, subject, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_EMAIL', 'Asset Assignment', 'Asset Assigned: {{assetId}}',
  'Hello {{name}}, asset {{assetId}} has been assigned to you.',
  '{"name":"Employee Name","assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_EMAIL', 'Asset Return', 'Asset {{assetId}} Returned',
  'Hi {{name}}, your return for asset {{assetId}} has been logged.',
  '{"name":"Employee Name","assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_EMAIL', 'Maintenance Alert', 'Maintenance Scheduled for Asset {{assetId}}',
  'Asset {{assetId}} is scheduled for maintenance on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_EMAIL', 'Asset System Error', 'Asset Error: {{errorCode}}',
  'Asset {{assetId}} encountered error {{errorCode}} at {{timestamp}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code","timestamp":"Error Time"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('WELCOME_EMAIL', 'Welcome Email', 'Welcome to Our Store',
  'Hello {{name}}, thank you for registering with us! Enjoy shopping 🎉',
  '{"name":"Customer Name"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_EMAIL', 'Order Confirmation', 'Order #{{orderId}} Confirmed',
  'Hi {{name}}, your order {{orderId}} has been successfully confirmed.',
  '{"name":"Customer Name","orderId":"Order ID"}', TRUE, 'ECOM'),

 ('SHIPMENT_EMAIL', 'Shipment Notification', 'Your Order #{{orderId}} is Shipped',
  'Hi {{name}}, your order {{orderId}} has been shipped. Track it here: {{trackingLink}}',
  '{"name":"Customer Name","orderId":"Order ID","trackingLink":"Tracking URL"}', TRUE, 'ECOM'),

 ('DELIVERY_EMAIL', 'Delivery Notification', 'Your Order #{{orderId}} Delivered',
  'Hi {{name}}, your order {{orderId}} has been delivered. We hope you enjoy your purchase 😊',
  '{"name":"Customer Name","orderId":"Order ID"}', TRUE, 'ECOM'),

 ('PASSWORD_RESET_EMAIL', 'Password Reset', 'Reset Your Password',
  'Hello {{name}}, we received a request to reset your password. Click here: {{resetLink}}',
  '{"name":"Customer Name","resetLink":"Password Reset Link"}', TRUE, 'ECOM'),

 ('ERROR_EMAIL', 'System Error Notification', 'Error Code: {{errorCode}}',
  'Dear Admin, error {{errorCode}} occurred at {{timestamp}}. Details: {{details}}',
  '{"errorCode":"Error Code","timestamp":"Error Time","details":"Error Details"}', TRUE, 'ECOM'),


 ('OTP_EMAIL', 'OTP Verification', 'OTP Verification',
  'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM');

-- In-App Notification Templates
INSERT INTO inapp_template_master 
(template_code, name, title, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_INAPP', 'Asset Assignment', 'Asset Assigned',
  '📌 Asset {{assetId}} has been assigned to you, {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_INAPP', 'Asset Return', 'Asset Returned',
  '↩️ Asset {{assetId}} returned successfully by {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_INAPP', 'Maintenance Alert', 'Maintenance Scheduled',
  '⚙️ Asset {{assetId}} is scheduled for maintenance on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_INAPP', 'Asset Error', 'Asset Error Notification',
  '⚠️ Asset {{assetId}} error {{errorCode}} at {{timestamp}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code","timestamp":"Error Time"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('WELCOME_INAPP', 'Welcome Notification', 'Welcome to Our Store',
  '👋 Hi {{name}}, thanks for registering! Enjoy shopping 🎉',
  '{"name":"Customer Name"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_INAPP', 'Order Confirmation', 'Order Confirmed',
  '✅ Your order {{orderId}} has been confirmed.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('SHIPMENT_INAPP', 'Shipment Notification', 'Order Shipped',
  '📦 Your order {{orderId}} has been shipped. Track here: {{trackingLink}}',
  '{"orderId":"Order ID","trackingLink":"Tracking URL"}', TRUE, 'ECOM'),

 ('DELIVERY_INAPP', 'Delivery Notification', 'Order Delivered',
  '🎉 Your order {{orderId}} has been delivered successfully.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('PASSWORD_RESET_INAPP', 'Password Reset', 'Password Reset Requested',
  'Hello {{name}}, a password reset was requested. Reset it here: {{resetLink}}',
  '{"name":"Customer Name","resetLink":"Password Reset Link"}', TRUE, 'ECOM'),

 ('ERROR_INAPP', 'System Error Notification', 'System Error',
  '⚠️ Error {{errorCode}} occurred at {{timestamp}}. Details: {{details}}',
  '{"errorCode":"Error Code","timestamp":"Error Time","details":"Error Details"}', TRUE, 'ECOM'),

 ('OTP_INAPP', 'OTP Verification', 'OTP Verification',
  'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM');


-- =======================================================================
-- ASSET MANAGEMENT SYSTEM - FULL TEMPLATE SEED (ALL MODULES + ALL CHANNELS)
-- =======================================================================
-- Includes 14 modules × 4 notification channels × CRUD events
-- Project Type: ASSET_MGMT
-- Author: Generated by ChatGPT
-- =======================================================================

-- =======================================================================
-- 1. ASSET CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('ASSET_CREATED_EMAIL', 'Asset Created', 'New Asset: {{assetName}}', '✅ Asset {{assetName}} created successfully by {{username}}.', '{"assetName":"Asset Name","username":"Created By"}', 'ASSET_MGMT'),
('ASSET_UPDATED_EMAIL', 'Asset Updated', 'Asset Updated: {{assetName}}', '✏️ Asset {{assetName}} updated successfully by {{username}}.', '{"assetName":"Asset Name","username":"Updated By"}', 'ASSET_MGMT'),
('ASSET_DELETED_EMAIL', 'Asset Deleted', 'Asset Deleted: {{assetName}}', '🗑️ Asset {{assetName}} deleted by {{username}}.', '{"assetName":"Asset Name","username":"Deleted By"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, body, placeholders, project_type) VALUES
('ASSET_CREATED_SMS', 'Asset Created', 'Asset {{assetName}} created successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT'),
('ASSET_UPDATED_SMS', 'Asset Updated', 'Asset {{assetName}} updated successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT'),
('ASSET_DELETED_SMS', 'Asset Deleted', 'Asset {{assetName}} deleted successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('ASSET_CREATED_WA', 'Asset Created', 'Asset Created', '✅ Asset {{assetName}} created successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT'),
('ASSET_UPDATED_WA', 'Asset Updated', 'Asset Updated', '✏️ Asset {{assetName}} updated successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT'),
('ASSET_DELETED_WA', 'Asset Deleted', 'Asset Deleted', '🗑️ Asset {{assetName}} deleted successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, title, body, placeholders, project_type) VALUES
('ASSET_CREATED_INAPP', 'Asset Created', 'Asset Created', '✅ Asset {{assetName}} created successfully by {{username}}.', '{"assetName":"Asset Name","username":"Created By"}', 'ASSET_MGMT'),
('ASSET_UPDATED_INAPP', 'Asset Updated', 'Asset Updated', '✏️ Asset {{assetName}} updated successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT'),
('ASSET_DELETED_INAPP', 'Asset Deleted', 'Asset Deleted', '🗑️ Asset {{assetName}} deleted successfully.', '{"assetName":"Asset Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 2. CATEGORY CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('CATEGORY_CREATED_EMAIL', 'Category Created', 'Category Created: {{categoryName}}', '📁 Category {{categoryName}} created successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_UPDATED_EMAIL', 'Category Updated', 'Category Updated: {{categoryName}}', '✏️ Category {{categoryName}} updated successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_DELETED_EMAIL', 'Category Deleted', 'Category Deleted: {{categoryName}}', '🗑️ Category {{categoryName}} deleted successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('CATEGORY_CREATED_SMS', 'Category Created', 'Category Created: {{categoryName}}', '📁 Category {{categoryName}} created successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_UPDATED_SMS', 'Category Updated', 'Category Updated: {{categoryName}}', '✏️ Category {{categoryName}} updated successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_DELETED_SMS', 'Category Deleted', 'Category Deleted: {{categoryName}}', '🗑️ Category {{categoryName}} deleted successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('CATEGORY_CREATED_WA', 'Category Created', 'Category Created: {{categoryName}}', '📁 Category {{categoryName}} created successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_UPDATED_WA', 'Category Updated', 'Category Updated: {{categoryName}}', '✏️ Category {{categoryName}} updated successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_DELETED_WA', 'Category Deleted', 'Category Deleted: {{categoryName}}', '🗑️ Category {{categoryName}} deleted successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('CATEGORY_CREATED_INAPP', 'Category Created', 'Category Created: {{categoryName}}', '📁 Category {{categoryName}} created successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_UPDATED_INAPP', 'Category Updated', 'Category Updated: {{categoryName}}', '✏️ Category {{categoryName}} updated successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT'),
('CATEGORY_DELETED_INAPP', 'Category Deleted', 'Category Deleted: {{categoryName}}', '🗑️ Category {{categoryName}} deleted successfully.', '{"categoryName":"Category Name"}', 'ASSET_MGMT');


-- =======================================================================
-- 3. SUBCATEGORY CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('SUBCATEGORY_CREATED_EMAIL', 'SubCategory Created', 'SubCategory Created: {{subCategoryName}}', '📦 SubCategory {{subCategoryName}} created successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_UPDATED_EMAIL', 'SubCategory Updated', 'SubCategory Updated: {{subCategoryName}}', '✏️ SubCategory {{subCategoryName}} updated successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_DELETED_EMAIL', 'SubCategory Deleted', 'SubCategory Deleted: {{subCategoryName}}', '🗑️ SubCategory {{subCategoryName}} deleted successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('SUBCATEGORY_CREATED_SMS', 'SubCategory Created', 'SubCategory Created: {{subCategoryName}}', '📦 SubCategory {{subCategoryName}} created successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_UPDATED_SMS', 'SubCategory Updated', 'SubCategory Updated: {{subCategoryName}}', '✏️ SubCategory {{subCategoryName}} updated successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_DELETED_SMS', 'SubCategory Deleted', 'SubCategory Deleted: {{subCategoryName}}', '🗑️ SubCategory {{subCategoryName}} deleted successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('SUBCATEGORY_CREATED_WA', 'SubCategory Created', 'SubCategory Created: {{subCategoryName}}', '📦 SubCategory {{subCategoryName}} created successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_UPDATED_WA', 'SubCategory Updated', 'SubCategory Updated: {{subCategoryName}}', '✏️ SubCategory {{subCategoryName}} updated successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_DELETED_WA', 'SubCategory Deleted', 'SubCategory Deleted: {{subCategoryName}}', '🗑️ SubCategory {{subCategoryName}} deleted successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('SUBCATEGORY_CREATED_INAPP', 'SubCategory Created', 'SubCategory Created: {{subCategoryName}}', '📦 SubCategory {{subCategoryName}} created successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_UPDATED_INAPP', 'SubCategory Updated', 'SubCategory Updated: {{subCategoryName}}', '✏️ SubCategory {{subCategoryName}} updated successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT'),
('SUBCATEGORY_DELETED_INAPP', 'SubCategory Deleted', 'SubCategory Deleted: {{subCategoryName}}', '🗑️ SubCategory {{subCategoryName}} deleted successfully.', '{"subCategoryName":"SubCategory Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 4. COMPONENT CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('COMPONENT_CREATED_EMAIL', 'Component Created', 'Component Created: {{componentName}}', '🧩 Component {{componentName}} created successfully by {{username}}.', '{"componentName":"Component Name","username":"Created By"}', 'ASSET_MGMT'),
('COMPONENT_UPDATED_EMAIL', 'Component Updated', 'Component Updated: {{componentName}}', '✏️ Component {{componentName}} updated successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT'),
('COMPONENT_DELETED_EMAIL', 'Component Deleted', 'Component Deleted: {{componentName}}', '🗑️ Component {{componentName}} deleted successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('COMPONENT_CREATED_SMS', 'Component Created', 'Component Created: {{componentName}}', '🧩 Component {{componentName}} created successfully by {{username}}.', '{"componentName":"Component Name","username":"Created By"}', 'ASSET_MGMT'),
('COMPONENT_UPDATED_SMS', 'Component Updated', 'Component Updated: {{componentName}}', '✏️ Component {{componentName}} updated successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT'),
('COMPONENT_DELETED_SMS', 'Component Deleted', 'Component Deleted: {{componentName}}', '🗑️ Component {{componentName}} deleted successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('COMPONENT_CREATED_WA', 'Component Created', 'Component Created: {{componentName}}', '🧩 Component {{componentName}} created successfully by {{username}}.', '{"componentName":"Component Name","username":"Created By"}', 'ASSET_MGMT'),
('COMPONENT_UPDATED_WA', 'Component Updated', 'Component Updated: {{componentName}}', '✏️ Component {{componentName}} updated successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT'),
('COMPONENT_DELETED_WA', 'Component Deleted', 'Component Deleted: {{componentName}}', '🗑️ Component {{componentName}} deleted successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('COMPONENT_CREATED_INAPP', 'Component Created', 'Component Created: {{componentName}}', '🧩 Component {{componentName}} created successfully by {{username}}.', '{"componentName":"Component Name","username":"Created By"}', 'ASSET_MGMT'),
('COMPONENT_UPDATED_INAPP', 'Component Updated', 'Component Updated: {{componentName}}', '✏️ Component {{componentName}} updated successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT'),
('COMPONENT_DELETED_INAPP', 'Component Deleted', 'Component Deleted: {{componentName}}', '🗑️ Component {{componentName}} deleted successfully.', '{"componentName":"Component Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 5. MAKE CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MAKE_CREATED_EMAIL', 'Make Created', 'Make Created: {{makeName}}', '🏭 Make {{makeName}} created successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_UPDATED_EMAIL', 'Make Updated', 'Make Updated: {{makeName}}', '✏️ Make {{makeName}} updated successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_DELETED_EMAIL', 'Make Deleted', 'Make Deleted: {{makeName}}', '🗑️ Make {{makeName}} deleted successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MAKE_CREATED_SMS', 'Make Created', 'Make Created: {{makeName}}', '🏭 Make {{makeName}} created successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_UPDATED_SMS', 'Make Updated', 'Make Updated: {{makeName}}', '✏️ Make {{makeName}} updated successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_DELETED_SMS', 'Make Deleted', 'Make Deleted: {{makeName}}', '🗑️ Make {{makeName}} deleted successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MAKE_CREATED_WA', 'Make Created', 'Make Created: {{makeName}}', '🏭 Make {{makeName}} created successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_UPDATED_WA', 'Make Updated', 'Make Updated: {{makeName}}', '✏️ Make {{makeName}} updated successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_DELETED_WA', 'Make Deleted', 'Make Deleted: {{makeName}}', '🗑️ Make {{makeName}} deleted successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MAKE_CREATED_INAPP', 'Make Created', 'Make Created: {{makeName}}', '🏭 Make {{makeName}} created successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_UPDATED_INAPP', 'Make Updated', 'Make Updated: {{makeName}}', '✏️ Make {{makeName}} updated successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT'),
('MAKE_DELETED_INAPP', 'Make Deleted', 'Make Deleted: {{makeName}}', '🗑️ Make {{makeName}} deleted successfully.', '{"makeName":"Make Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 6. MODEL CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MODEL_CREATED_EMAIL', 'Model Created', 'Model Created: {{modelName}}', '🧱 Model {{modelName}} created successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_UPDATED_EMAIL', 'Model Updated', 'Model Updated: {{modelName}}', '✏️ Model {{modelName}} updated successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_DELETED_EMAIL', 'Model Deleted', 'Model Deleted: {{modelName}}', '🗑️ Model {{modelName}} deleted successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MODEL_CREATED_SMS', 'Model Created', 'Model Created: {{modelName}}', '🧱 Model {{modelName}} created successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_UPDATED_SMS', 'Model Updated', 'Model Updated: {{modelName}}', '✏️ Model {{modelName}} updated successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_DELETED_SMS', 'Model Deleted', 'Model Deleted: {{modelName}}', '🗑️ Model {{modelName}} deleted successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MODEL_CREATED_WA', 'Model Created', 'Model Created: {{modelName}}', '🧱 Model {{modelName}} created successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_UPDATED_WA', 'Model Updated', 'Model Updated: {{modelName}}', '✏️ Model {{modelName}} updated successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_DELETED_WA', 'Model Deleted', 'Model Deleted: {{modelName}}', '🗑️ Model {{modelName}} deleted successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('MODEL_CREATED_INAPP', 'Model Created', 'Model Created: {{modelName}}', '🧱 Model {{modelName}} created successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_UPDATED_INAPP', 'Model Updated', 'Model Updated: {{modelName}}', '✏️ Model {{modelName}} updated successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT'),
('MODEL_DELETED_INAPP', 'Model Deleted', 'Model Deleted: {{modelName}}', '🗑️ Model {{modelName}} deleted successfully.', '{"modelName":"Model Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 7. VENDOR CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('VENDOR_CREATED_EMAIL', 'Vendor Created', 'Vendor Created: {{vendorName}}', '🏢 Vendor {{vendorName}} registered successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_UPDATED_EMAIL', 'Vendor Updated', 'Vendor Updated: {{vendorName}}', '✏️ Vendor {{vendorName}} updated successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_DELETED_EMAIL', 'Vendor Deleted', 'Vendor Deleted: {{vendorName}}', '🗑️ Vendor {{vendorName}} deleted successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('VENDOR_CREATED_SMS', 'Vendor Created', 'Vendor Created: {{vendorName}}', '🏢 Vendor {{vendorName}} registered successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_UPDATED_SMS', 'Vendor Updated', 'Vendor Updated: {{vendorName}}', '✏️ Vendor {{vendorName}} updated successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_DELETED_SMS', 'Vendor Deleted', 'Vendor Deleted: {{vendorName}}', '🗑️ Vendor {{vendorName}} deleted successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('VENDOR_CREATED_WA', 'Vendor Created', 'Vendor Created: {{vendorName}}', '🏢 Vendor {{vendorName}} registered successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_UPDATED_WA', 'Vendor Updated', 'Vendor Updated: {{vendorName}}', '✏️ Vendor {{vendorName}} updated successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_DELETED_WA', 'Vendor Deleted', 'Vendor Deleted: {{vendorName}}', '🗑️ Vendor {{vendorName}} deleted successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('VENDOR_CREATED_INAPP', 'Vendor Created', 'Vendor Created: {{vendorName}}', '🏢 Vendor {{vendorName}} registered successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_UPDATED_INAPP', 'Vendor Updated', 'Vendor Updated: {{vendorName}}', '✏️ Vendor {{vendorName}} updated successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT'),
('VENDOR_DELETED_INAPP', 'Vendor Deleted', 'Vendor Deleted: {{vendorName}}', '🗑️ Vendor {{vendorName}} deleted successfully.', '{"vendorName":"Vendor Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 8. OUTLET CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('OUTLET_CREATED_EMAIL', 'Outlet Created', 'Outlet Created: {{outletName}}', '🏬 Outlet {{outletName}} created successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_UPDATED_EMAIL', 'Outlet Updated', 'Outlet Updated: {{outletName}}', '✏️ Outlet {{outletName}} updated successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_DELETED_EMAIL', 'Outlet Deleted', 'Outlet Deleted: {{outletName}}', '🗑️ Outlet {{outletName}} deleted successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('OUTLET_CREATED_SMS', 'Outlet Created', 'Outlet Created: {{outletName}}', '🏬 Outlet {{outletName}} created successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_UPDATED_SMS', 'Outlet Updated', 'Outlet Updated: {{outletName}}', '✏️ Outlet {{outletName}} updated successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_DELETED_SMS', 'Outlet Deleted', 'Outlet Deleted: {{outletName}}', '🗑️ Outlet {{outletName}} deleted successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('OUTLET_CREATED_WA', 'Outlet Created', 'Outlet Created: {{outletName}}', '🏬 Outlet {{outletName}} created successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_UPDATED_WA', 'Outlet Updated', 'Outlet Updated: {{outletName}}', '✏️ Outlet {{outletName}} updated successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_DELETED_WA', 'Outlet Deleted', 'Outlet Deleted: {{outletName}}', '🗑️ Outlet {{outletName}} deleted successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('OUTLET_CREATED_INAPP', 'Outlet Created', 'Outlet Created: {{outletName}}', '🏬 Outlet {{outletName}} created successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_UPDATED_INAPP', 'Outlet Updated', 'Outlet Updated: {{outletName}}', '✏️ Outlet {{outletName}} updated successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT'),
('OUTLET_DELETED_INAPP', 'Outlet Deleted', 'Outlet Deleted: {{outletName}}', '🗑️ Outlet {{outletName}} deleted successfully.', '{"outletName":"Outlet Name"}', 'ASSET_MGMT');

-- =======================================================================
-- 9. AMC CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AMC_CREATED_EMAIL', 'AMC Created', 'AMC Created for Asset {{assetId}}', '📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('AMC_UPDATED_EMAIL', 'AMC Updated', 'AMC Updated for Asset {{assetId}}', '✏️ AMC details updated for asset {{assetId}}.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('AMC_DELETED_EMAIL', 'AMC Deleted', 'AMC Deleted for Asset {{assetId}}', '🗑️ AMC for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AMC_CREATED_SMS', 'AMC Created', 'AMC Created for Asset {{assetId}}', '📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('AMC_UPDATED_SMS', 'AMC Updated', 'AMC Updated for Asset {{assetId}}', '✏️ AMC details updated for asset {{assetId}}.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('AMC_DELETED_SMS', 'AMC Deleted', 'AMC Deleted for Asset {{assetId}}', '🗑️ AMC for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AMC_CREATED_WA', 'AMC Created', 'AMC Created for Asset {{assetId}}', '📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('AMC_UPDATED_WA', 'AMC Updated', 'AMC Updated for Asset {{assetId}}', '✏️ AMC details updated for asset {{assetId}}.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('AMC_DELETED_WA', 'AMC Deleted', 'AMC Deleted for Asset {{assetId}}', '🗑️ AMC for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AMC_CREATED_INAPP', 'AMC Created', 'AMC Created for Asset {{assetId}}', '📅 AMC created for asset {{assetId}} valid from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('AMC_UPDATED_INAPP', 'AMC Updated', 'AMC Updated for Asset {{assetId}}', '✏️ AMC details updated for asset {{assetId}}.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('AMC_DELETED_INAPP', 'AMC Deleted', 'AMC Deleted for Asset {{assetId}}', '🗑️ AMC for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

-- =======================================================================
-- 10. WARRANTY CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('WARRANTY_CREATED_EMAIL', 'Warranty Created', 'Warranty Created for Asset {{assetId}}', '🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('WARRANTY_UPDATED_EMAIL', 'Warranty Updated', 'Warranty Updated for Asset {{assetId}}', '✏️ Warranty for asset {{assetId}} updated successfully.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('WARRANTY_DELETED_EMAIL', 'Warranty Deleted', 'Warranty Deleted for Asset {{assetId}}', '🗑️ Warranty for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('WARRANTY_CREATED_SMS', 'Warranty Created', 'Warranty Created for Asset {{assetId}}', '🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('WARRANTY_UPDATED_SMS', 'Warranty Updated', 'Warranty Updated for Asset {{assetId}}', '✏️ Warranty for asset {{assetId}} updated successfully.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('WARRANTY_DELETED_SMS', 'Warranty Deleted', 'Warranty Deleted for Asset {{assetId}}', '🗑️ Warranty for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('WARRANTY_CREATED_WA', 'Warranty Created', 'Warranty Created for Asset {{assetId}}', '🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('WARRANTY_UPDATED_WA', 'Warranty Updated', 'Warranty Updated for Asset {{assetId}}', '✏️ Warranty for asset {{assetId}} updated successfully.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('WARRANTY_DELETED_WA', 'Warranty Deleted', 'Warranty Deleted for Asset {{assetId}}', '🗑️ Warranty for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('WARRANTY_CREATED_INAPP', 'Warranty Created', 'Warranty Created for Asset {{assetId}}', '🛡️ Warranty created for asset {{assetId}} from {{startDate}} to {{endDate}}.', '{"assetId":"Asset ID","startDate":"Start Date","endDate":"End Date"}', 'ASSET_MGMT'),
('WARRANTY_UPDATED_INAPP', 'Warranty Updated', 'Warranty Updated for Asset {{assetId}}', '✏️ Warranty for asset {{assetId}} updated successfully.', '{"assetId":"Asset ID"}', 'ASSET_MGMT'),
('WARRANTY_DELETED_INAPP', 'Warranty Deleted', 'Warranty Deleted for Asset {{assetId}}', '🗑️ Warranty for asset {{assetId}} deleted.', '{"assetId":"Asset ID"}', 'ASSET_MGMT');

-- =======================================================================
-- 11. DOCUMENT CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('DOCUMENT_UPLOADED_EMAIL', 'Document Uploaded', 'Document Uploaded for Asset {{assetId}}', '📎 Document {{fileName}} uploaded for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT'),
('DOCUMENT_DELETED_EMAIL', 'Document Deleted', 'Document Deleted for Asset {{assetId}}', '🗑️ Document {{fileName}} deleted for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('DOCUMENT_UPLOADED_SMS', 'Document Uploaded', 'Document Uploaded for Asset {{assetId}}', '📎 Document {{fileName}} uploaded for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT'),
('DOCUMENT_DELETED_SMS', 'Document Deleted', 'Document Deleted for Asset {{assetId}}', '🗑️ Document {{fileName}} deleted for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('DOCUMENT_UPLOADED_WA', 'Document Uploaded', 'Document Uploaded for Asset {{assetId}}', '📎 Document {{fileName}} uploaded for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT'),
('DOCUMENT_DELETED_WA', 'Document Deleted', 'Document Deleted for Asset {{assetId}}', '🗑️ Document {{fileName}} deleted for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('DOCUMENT_UPLOADED_INAPP', 'Document Uploaded', 'Document Uploaded for Asset {{assetId}}', '📎 Document {{fileName}} uploaded for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT'),
('DOCUMENT_DELETED_INAPP', 'Document Deleted', 'Document Deleted for Asset {{assetId}}', '🗑️ Document {{fileName}} deleted for asset {{assetId}}.', '{"fileName":"File Name","assetId":"Asset ID"}', 'ASSET_MGMT');

-- =======================================================================
-- 12. USERLINK CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('USERLINK_CREATED_EMAIL', 'User Link Created', 'User Link Created', '🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.', '{"username":"User Name","assetId":"Asset ID","subCategory":"SubCategory"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('USERLINK_CREATED_SMS', 'User Link Created', 'User Link Created', '🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.', '{"username":"User Name","assetId":"Asset ID","subCategory":"SubCategory"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('USERLINK_CREATED_WA', 'User Link Created', 'User Link Created', '🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.', '{"username":"User Name","assetId":"Asset ID","subCategory":"SubCategory"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('USERLINK_CREATED_INAP', 'User Link Created', 'User Link Created', '🔗 User {{username}} linked to asset {{assetId}} under subcategory {{subCategory}}.', '{"username":"User Name","assetId":"Asset ID","subCategory":"SubCategory"}', 'ASSET_MGMT');

-- =======================================================================
-- 13. AUDIT CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AUDIT_ENTRY_EMAIL', 'Audit Log Entry', 'New Audit Log Entry', '🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.', '{"action":"Action","entityName":"Entity","entityId":"ID","username":"Actor"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AUDIT_ENTRY_SMS', 'Audit Log Entry', 'New Audit Log Entry', '🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.', '{"action":"Action","entityName":"Entity","entityId":"ID","username":"Actor"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AUDIT_ENTRY_WA', 'Audit Log Entry', 'New Audit Log Entry', '🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.', '{"action":"Action","entityName":"Entity","entityId":"ID","username":"Actor"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('AUDIT_ENTRY_INAPP', 'Audit Log Entry', 'New Audit Log Entry', '🧾 Action {{action}} performed on {{entityName}} (ID: {{entityId}}) by {{username}}.', '{"action":"Action","entityName":"Entity","entityId":"ID","username":"Actor"}', 'ASSET_MGMT');

-- =======================================================================
-- 14. FILEDOWNLOAD CONTROLLER
-- =======================================================================
INSERT INTO notification_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('FILE_DOWNLOAD_EMAIL', 'File Download', 'File Downloaded', '📥 File {{fileName}} downloaded successfully by {{username}}.', '{"fileName":"File Name","username":"Downloaded By"}', 'ASSET_MGMT');

INSERT INTO sms_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('FILE_DOWNLOAD_SMS', 'File Download', 'File Downloaded', '📥 File {{fileName}} downloaded successfully by {{username}}.', '{"fileName":"File Name","username":"Downloaded By"}', 'ASSET_MGMT');

INSERT INTO whatsapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('FILE_DOWNLOAD_WA', 'File Download', 'File Downloaded', '📥 File {{fileName}} downloaded successfully by {{username}}.', '{"fileName":"File Name","username":"Downloaded By"}', 'ASSET_MGMT');

INSERT INTO inapp_template_master (template_code, name, subject, body, placeholders, project_type) VALUES
('FILE_DOWNLOAD_INAPP', 'File Download', 'File Downloaded', '📥 File {{fileName}} downloaded successfully by {{username}}.', '{"fileName":"File Name","username":"Downloaded By"}', 'ASSET_MGMT');

-- Select * from  sms_template_master;  Select * from  whatsapp_template_master;  Select * from notification_template_master;
