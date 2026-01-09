# Notification Service Postman Collection Generation

## Overview
This collection includes all notification templates from the seed script, organized by:
- **Channels**: SMS, EMAIL, WHATSAPP, INAPP
- **Project Types**: ASSET_MGMT, ECOM
- **Entities**: Asset, Category, SubCategory, Component, Make, Model, Vendor, Outlet, AMC, Warranty, Document, UserLink, Audit, FileDownload

## Total Templates
- SMS: 44 templates
- EMAIL: 46 templates  
- WHATSAPP: 45 templates
- INAPP: 45 templates
- **Total: 180+ templates**

## Generation
Run the Python script to generate the complete collection:
```bash
cd notification-service/docs/postman
python3 generate_collection.py
```

This will create `Notification_Service_API.postman_collection.json` with all templates organized properly.

## Environment Variables
All templates use environment variables from `Notification_Service_Environment.postman_environment.json`:
- `notificationbaseUrl`, `accessToken`, `projectType`
- Entity-specific: `assetId`, `assetName`, `categoryName`, etc.
- Common: `mobile`, `email`, `userId`, `username`, `otp`, etc.

## Structure
1. SMS Notifications
   - ASSET_MGMT (by entity)
   - ECOM
2. EMAIL Notifications
   - ASSET_MGMT (by entity)
   - ECOM
3. WHATSAPP Notifications
   - ASSET_MGMT (by entity)
   - ECOM
4. INAPP Notifications
   - ASSET_MGMT (by entity)
   - ECOM

