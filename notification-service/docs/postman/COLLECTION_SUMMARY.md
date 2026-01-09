# Notification Service Postman Collection - Summary

## Overview
Comprehensive Postman collection for Notification Service API based on entity models and seed templates from `V2__seed_templates.sql`.

## Statistics
- **Total Requests**: 181 notification requests
- **Channels**: 4 (SMS, EMAIL, WHATSAPP, INAPP)
- **Project Types**: 2 (ASSET_MGMT, ECOM)
- **File Size**: ~152KB

## Structure

### 1. SMS Notifications (44 requests)
- **ASSET_MGMT - SMS**: 39 requests
  - Asset Operations (CREATED, UPDATED, DELETED, ASSIGN, RETURN, MAINT, ERROR)
  - Category, SubCategory, Component, Make, Model, Vendor, Outlet
  - AMC, Warranty, Document, UserLink, Audit, FileDownload
- **ECOM - SMS**: 5 requests
  - OTP, Order Confirmation, Shipment, Delivery, Error

### 2. EMAIL Notifications (46 requests)
- **ASSET_MGMT - EMAIL**: 39 requests
  - Same entities as SMS
- **ECOM - EMAIL**: 7 requests
  - OTP, Welcome, Order Confirmation, Shipment, Delivery, Password Reset, Error

### 3. WHATSAPP Notifications (45 requests)
- **ASSET_MGMT - WHATSAPP**: 39 requests
  - Same entities as SMS
- **ECOM - WHATSAPP**: 6 requests
  - Welcome, Order Confirmation, Shipment, Delivery, Alert, OTP

### 4. INAPP Notifications (46 requests)
- **ASSET_MGMT - INAPP**: 39 requests
  - Same entities as SMS
- **ECOM - INAPP**: 7 requests
  - Welcome, Order Confirmation, Shipment, Delivery, Password Reset, Error, OTP

## Environment Variables

All requests use environment variables from `Notification_Service_Environment.postman_environment.json`:

### Base Variables
- `notificationbaseUrl`: http://localhost:8082
- `accessToken`: JWT Bearer token
- `projectType`: ASSET_MGMT or ECOM
- `channel`: SMS, EMAIL, WHATSAPP, INAPP

### Recipient Variables
- `mobile`: +919876543210 (for SMS/WhatsApp)
- `email`: user@example.com (for Email)
- `userId`: 1 (for In-App)

### Entity Variables
- `assetId`, `assetName`
- `categoryName`, `subCategoryName`
- `componentName`, `makeName`, `modelName`
- `vendorName`, `outletName`
- `startDate`, `endDate` (for AMC/Warranty)
- `fileName` (for Document/FileDownload)
- `username`, `name`
- `orderId`, `otp`, `errorCode`, `timestamp`
- `trackingLink`, `resetLink`, `activationLink`
- `priority`: LOW, NORMAL, HIGH, URGENT

## Template Categories

### Asset Management (ASSET_MGMT)
1. **Asset**: CREATED, UPDATED, DELETED, ASSIGN, RETURN, MAINT, ERROR
2. **Category**: CREATED, UPDATED, DELETED
3. **SubCategory**: CREATED, UPDATED, DELETED
4. **Component**: CREATED, UPDATED, DELETED
5. **Make**: CREATED, UPDATED, DELETED
6. **Model**: CREATED, UPDATED, DELETED
7. **Vendor**: CREATED, UPDATED, DELETED
8. **Outlet**: CREATED, UPDATED, DELETED
9. **AMC**: CREATED, UPDATED, DELETED
10. **Warranty**: CREATED, UPDATED, DELETED
11. **Document**: UPLOADED, DELETED
12. **UserLink**: CREATED
13. **Audit**: ENTRY
14. **FileDownload**: DOWNLOAD

### E-Commerce (ECOM)
- OTP (SMS, EMAIL, WHATSAPP, INAPP)
- Welcome (EMAIL, WHATSAPP, INAPP)
- Order Confirmation (SMS, EMAIL, WHATSAPP, INAPP)
- Shipment (SMS, EMAIL, WHATSAPP, INAPP)
- Delivery (SMS, EMAIL, WHATSAPP, INAPP)
- Password Reset (EMAIL, INAPP)
- Error (SMS, EMAIL, INAPP)
- Alert (WHATSAPP)

## Usage

1. **Import Collection**: Import `Notification_Service_API.postman_collection.json`
2. **Import Environment**: Import `Notification_Service_Environment.postman_environment.json`
3. **Select Environment**: Choose "Notification Service - Local" from dropdown
4. **Update Variables**: Modify environment variables as needed
5. **Execute Requests**: All requests use environment variables automatically

## Example Request

```json
{
  "channel": "SMS",
  "templateCode": "ASSET_CREATED_SMS",
  "recipient": "{{mobile}}",
  "variables": {
    "assetName": "{{assetName}}"
  },
  "priority": "NORMAL",
  "metadata": {
    "source": "asset-service",
    "projectType": "ASSET_MGMT",
    "eventType": "ASSET_CREATED_SMS"
  }
}
```

## Response

All requests return:
- **Status**: 202 Accepted
- **Body**: "{CHANNEL} Notification accepted"

## Notes

- All templates are based on seed data from `V2__seed_templates.sql`
- Templates use dynamic variable substitution
- Priority is automatically set based on template type (ERROR/ALERT = URGENT, OTP/PASSWORD_RESET = HIGH, others = NORMAL)
- Metadata includes source service, project type, and event type for tracking
- All requests require JWT Bearer token in Authorization header

## Generation

The collection is generated using `generate_collection.py` script which:
1. Extracts all templates from seed SQL file
2. Organizes by channel and project type
3. Generates request items with proper variables
4. Includes example responses
5. Uses environment variables throughout

To regenerate:
```bash
cd notification-service/docs/postman
python3 generate_collection.py
```

