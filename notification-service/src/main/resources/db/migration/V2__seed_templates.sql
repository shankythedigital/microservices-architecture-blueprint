
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

-- Select * from  sms_template_master;  Select * from  whatsapp_template_master;  Select * from notification_template_master;
