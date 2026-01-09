#!/usr/bin/env python3
"""
Generate comprehensive Postman collection for Notification Service
based on entity models and seed templates.
"""

import json
import re
from collections import defaultdict

def extract_templates_from_seed():
    """Extract all templates from seed file"""
    import os
    seed_path = os.path.join(os.path.dirname(__file__), '../../src/main/resources/db/migration/V2__seed_templates.sql')
    with open(seed_path, 'r') as f:
        content = f.read()
    
    templates = defaultdict(lambda: defaultdict(list))
    
    # Extract SMS templates - find all template codes
    sms_codes = re.findall(r"'([A-Z_]+_SMS)'", content)
    for code in sms_codes:
        # Find project type for this template
        pattern = rf"'{code}'[^)]+'([A-Z_]+)'\s*\)"
        project_match = re.search(pattern, content)
        project = project_match.group(1) if project_match else 'ASSET_MGMT'
        if project in ['ASSET_MGMT', 'ECOM']:
            templates['SMS'][project].append(code)
    
    # Extract Email templates - find all template codes
    email_codes = re.findall(r"'([A-Z_]+_EMAIL)'", content)
    for code in email_codes:
        # Find project type for this template
        pattern = rf"'{code}'[^)]+'([A-Z_]+)'\s*\)"
        project_match = re.search(pattern, content)
        project = project_match.group(1) if project_match else 'ASSET_MGMT'
        if project in ['ASSET_MGMT', 'ECOM']:
            templates['EMAIL'][project].append(code)
    
    # Extract WhatsApp templates - find all template codes
    wa_codes = re.findall(r"'([A-Z_]+_WA)'", content)
    for code in wa_codes:
        # Find project type for this template
        pattern = rf"'{code}'[^)]+'([A-Z_]+)'\s*\)"
        project_match = re.search(pattern, content)
        project = project_match.group(1) if project_match else 'ASSET_MGMT'
        if project in ['ASSET_MGMT', 'ECOM']:
            templates['WHATSAPP'][project].append(code)
    
    # Extract In-App templates - find all template codes (including _INAP variant)
    inapp_codes = re.findall(r"'([A-Z_]+_INAPP?)'", content)
    for code in inapp_codes:
        # Find project type for this template
        pattern = rf"'{code}'[^)]+'([A-Z_]+)'\s*\)"
        project_match = re.search(pattern, content)
        project = project_match.group(1) if project_match else 'ASSET_MGMT'
        if project in ['ASSET_MGMT', 'ECOM']:
            templates['INAPP'][project].append(code)
    
    return templates

def get_template_variables(template_code):
    """Get variables for a template based on common patterns"""
    variables = {}
    
    if 'OTP' in template_code:
        variables['otp'] = '{{otp}}'
    if 'ASSET' in template_code:
        variables['assetId'] = '{{assetId}}'
        variables['assetName'] = '{{assetName}}'
    if 'CATEGORY' in template_code:
        variables['categoryName'] = '{{categoryName}}'
    if 'SUBCATEGORY' in template_code:
        variables['subCategoryName'] = '{{subCategoryName}}'
    if 'COMPONENT' in template_code:
        variables['componentName'] = '{{componentName}}'
    if 'MAKE' in template_code:
        variables['makeName'] = '{{makeName}}'
    if 'MODEL' in template_code:
        variables['modelName'] = '{{modelName}}'
    if 'VENDOR' in template_code:
        variables['vendorName'] = '{{vendorName}}'
    if 'OUTLET' in template_code:
        variables['outletName'] = '{{outletName}}'
    if 'AMC' in template_code or 'WARRANTY' in template_code:
        variables['assetId'] = '{{assetId}}'
        variables['startDate'] = '{{startDate}}'
        variables['endDate'] = '{{endDate}}'
    if 'DOCUMENT' in template_code:
        variables['fileName'] = '{{fileName}}'
        variables['assetId'] = '{{assetId}}'
    if 'USERLINK' in template_code:
        variables['username'] = '{{username}}'
        variables['assetId'] = '{{assetId}}'
        variables['subCategory'] = '{{subCategoryName}}'
    if 'AUDIT' in template_code:
        variables['action'] = 'CREATE'
        variables['entityName'] = 'Asset'
        variables['entityId'] = '{{assetId}}'
        variables['username'] = '{{username}}'
    if 'FILE_DOWNLOAD' in template_code:
        variables['fileName'] = '{{fileName}}'
        variables['username'] = '{{username}}'
    if 'ORDER' in template_code:
        variables['orderId'] = '{{orderId}}'
    if 'SHIPMENT' in template_code:
        variables['orderId'] = '{{orderId}}'
        variables['trackingLink'] = '{{trackingLink}}'
    if 'DELIVERY' in template_code:
        variables['orderId'] = '{{orderId}}'
    if 'WELCOME' in template_code:
        variables['name'] = '{{name}}'
    if 'PASSWORD_RESET' in template_code:
        variables['name'] = '{{name}}'
        variables['resetLink'] = '{{resetLink}}'
    if 'ERROR' in template_code:
        variables['errorCode'] = '{{errorCode}}'
        variables['timestamp'] = '{{timestamp}}'
    if 'MAINT' in template_code:
        variables['assetId'] = '{{assetId}}'
        variables['date'] = '{{startDate}}'
    if 'ASSIGN' in template_code or 'RETURN' in template_code:
        variables['assetId'] = '{{assetId}}'
        variables['name'] = '{{name}}'
    
    # Add common fields
    if 'CREATED' in template_code or 'UPDATED' in template_code or 'DELETED' in template_code:
        entity = template_code.split('_')[0]
        if entity == 'ASSET':
            variables['assetName'] = '{{assetName}}'
            if 'CREATED' in template_code or 'UPDATED' in template_code:
                variables['username'] = '{{username}}'
        elif entity == 'CATEGORY':
            variables['categoryName'] = '{{categoryName}}'
        elif entity == 'SUBCATEGORY':
            variables['subCategoryName'] = '{{subCategoryName}}'
        elif entity == 'COMPONENT':
            variables['componentName'] = '{{componentName}}'
            if 'CREATED' in template_code:
                variables['username'] = '{{username}}'
        elif entity == 'MAKE':
            variables['makeName'] = '{{makeName}}'
        elif entity == 'MODEL':
            variables['modelName'] = '{{modelName}}'
        elif entity == 'VENDOR':
            variables['vendorName'] = '{{vendorName}}'
        elif entity == 'OUTLET':
            variables['outletName'] = '{{outletName}}'
        elif entity == 'AMC':
            variables['assetId'] = '{{assetId}}'
            variables['startDate'] = '{{startDate}}'
            variables['endDate'] = '{{endDate}}'
        elif entity == 'WARRANTY':
            variables['assetId'] = '{{assetId}}'
            variables['startDate'] = '{{startDate}}'
            variables['endDate'] = '{{endDate}}'
    
    return variables

def create_request_item(template_code, channel, project_type):
    """Create a Postman request item for a template"""
    placeholders = get_template_variables(template_code)
    
    # Determine recipient fields based on channel (matching NotificationRequest DTO)
    request_body = {
        "channel": channel,
        "templateCode": template_code,
        "placeholders": placeholders,
        "username": "{{username}}",
        "userId": "{{userId}}"
    }
    
    # Set mobile/email based on channel (matching DTO fields)
    if channel == 'SMS' or channel == 'WHATSAPP':
        request_body['mobile'] = '{{mobile}}'
    elif channel == 'EMAIL':
        request_body['email'] = '{{email}}'
    # For INAPP, userId is already set above, no mobile/email needed
    
    # Set subject/title based on channel
    if channel == 'EMAIL':
        request_body['subject'] = f"Subject for {template_code}"
    elif channel == 'INAPP':
        # For INAPP, subject field is used as title
        request_body['subject'] = f"Title for {template_code}"
    
    # Note: priority and metadata are not part of NotificationRequest DTO
    # They are removed to match the actual API contract
    
    description = f"Send {channel} notification using template {template_code}.\n\n"
    description += f"**Template Code:** {template_code}\n"
    description += f"**Project Type:** {project_type}\n"
    description += f"**Channel:** {channel}\n\n"
    description += "**Request Fields (NotificationRequest DTO):**\n"
    description += f"- `channel`: {channel} (required)\n"
    description += f"- `templateCode`: {template_code} (required)\n"
    if channel == 'SMS' or channel == 'WHATSAPP':
        description += f"- `mobile`: {{mobile}} (required for SMS/WhatsApp)\n"
    elif channel == 'EMAIL':
        description += f"- `email`: {{email}} (required for Email)\n"
    description += f"- `username`: {{username}} (optional, recommended)\n"
    description += f"- `userId`: {{userId}} (optional, for audit/logging)\n"
    if channel == 'EMAIL':
        description += f"- `subject`: Subject for {template_code} (optional, overrides template subject)\n"
    elif channel == 'INAPP':
        description += f"- `subject`: Title for {template_code} (optional, used as title)\n"
    description += f"- `placeholders`: Map of template variables (required)\n\n"
    description += "**Placeholders:**\n"
    for key, value in placeholders.items():
        description += f"- `{key}`: {value.replace('{{', '').replace('}}', '')}\n"
    
    return {
        "name": template_code.replace('_', ' ').title(),
        "request": {
            "method": "POST",
            "header": [
                {
                    "key": "Authorization",
                    "value": "Bearer {{accessToken}}",
                    "type": "text"
                },
                {
                    "key": "Content-Type",
                    "value": "application/json",
                    "type": "text"
                }
            ],
            "body": {
                "mode": "raw",
                "raw": json.dumps(request_body, indent=2),
                "options": {
                    "raw": {
                        "language": "json"
                    }
                }
            },
            "url": {
                "raw": "{{notificationbaseUrl}}/api/notifications",
                "host": ["{{notificationbaseUrl}}"],
                "path": ["api", "notifications"]
            },
            "description": description
        },
        "response": [
            {
                "name": f"Success - {channel} Queued",
                "originalRequest": {
                    "method": "POST",
                    "header": [
                        {
                            "key": "Authorization",
                            "value": "Bearer {{accessToken}}"
                        },
                        {
                            "key": "Content-Type",
                            "value": "application/json"
                        }
                    ],
                    "body": {
                        "mode": "raw",
                        "raw": json.dumps(request_body, indent=2),
                        "options": {
                            "raw": {
                                "language": "json"
                            }
                        }
                    },
                    "url": {
                        "raw": "{{notificationbaseUrl}}/api/notifications",
                        "host": ["{{notificationbaseUrl}}"],
                        "path": ["api", "notifications"]
                    }
                },
                "status": "Accepted",
                "code": 202,
                "_postman_previewlanguage": "text",
                "header": [
                    {
                        "key": "Content-Type",
                        "value": "text/plain;charset=UTF-8"
                    }
                ],
                "cookie": [],
                "body": f"{channel} Notification accepted"
            }
        ]
    }

def generate_collection():
    """Generate the complete Postman collection"""
    templates = extract_templates_from_seed()
    
    collection = {
        "info": {
            "_postman_id": "notification-service-api-complete",
            "name": "Notification Service API - Complete Collection",
            "description": "Comprehensive Postman collection for Notification Service API based on entity models and seed templates.\n\n**Key Features:**\n- Multi-channel notification support (SMS, Email, WhatsApp, In-App)\n- Template-based notifications with dynamic variable substitution\n- Priority levels (LOW, NORMAL, HIGH, URGENT)\n- Asynchronous processing\n- Metadata support\n- Organized by channel, project type, and entity\n\n**Environment Variables Required:**\n- notificationbaseUrl: Base URL (default: http://localhost:8082)\n- accessToken: JWT Bearer token from auth-service\n- projectType: Project type (ASSET_MGMT or ECOM)\n- Various entity-specific variables (assetId, assetName, etc.)",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "_exporter_id": "32725094"
        },
        "item": [],
        "variable": [
            {
                "key": "notificationbaseUrl",
                "value": "http://localhost:8082",
                "type": "string",
                "description": "Base URL for Notification Service API"
            },
            {
                "key": "accessToken",
                "value": "",
                "type": "string",
                "description": "JWT Bearer token from auth-service"
            },
            {
                "key": "projectType",
                "value": "ASSET_MGMT",
                "type": "string",
                "description": "Project type: ASSET_MGMT or ECOM"
            }
        ]
    }
    
    # Organize by channel
    channels = ['SMS', 'EMAIL', 'WHATSAPP', 'INAPP']
    project_types = ['ASSET_MGMT', 'ECOM']
    
    for channel in channels:
        channel_folder = {
            "name": f"{channel} Notifications",
            "item": []
        }
        
        for project_type in project_types:
            if project_type in templates[channel]:
                project_folder = {
                    "name": f"{project_type} - {channel}",
                    "item": []
                }
                
                # Sort templates
                template_list = sorted(templates[channel][project_type])
                
                for template_code in template_list:
                    request_item = create_request_item(template_code, channel, project_type)
                    project_folder["item"].append(request_item)
                
                channel_folder["item"].append(project_folder)
        
        collection["item"].append(channel_folder)
    
    return collection

if __name__ == "__main__":
    try:
        collection = generate_collection()
        output_file = 'Notification_Service_API.postman_collection.json'
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(collection, f, indent=2, ensure_ascii=False)
        print(f"✅ Collection generated successfully: {output_file}")
        print(f"   Total items: {len(collection['item'])} top-level folders")
    except Exception as e:
        print(f"❌ Error generating collection: {e}")
        import traceback
        traceback.print_exc()

