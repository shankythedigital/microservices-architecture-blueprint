#!/usr/bin/env python3
"""
Script to fix variable names and add missing endpoints to the consolidated collection.
"""

import json
import sys

def fix_variable_names(data):
    """Recursively fix baseUrl to assetbaseUrl"""
    if isinstance(data, dict):
        for key, value in data.items():
            if key == 'raw' and isinstance(value, str):
                data[key] = value.replace('{{baseUrl}}', '{{assetbaseUrl}}')
            elif key == 'host' and isinstance(value, list):
                data[key] = [v.replace('{{baseUrl}}', '{{assetbaseUrl}}') if isinstance(v, str) else v for v in value]
            else:
                fix_variable_names(value)
    elif isinstance(data, list):
        for item in data:
            fix_variable_names(item)
    elif isinstance(data, str):
        # This shouldn't happen but just in case
        pass

def add_missing_audit_endpoints(collection):
    """Add missing Audit Agent endpoints"""
    
    # Find Audit Agent folder
    audit_folder = None
    for item in collection['item']:
        if item.get('name') == '21. Audit Agent':
            audit_folder = item
            break
    
    if not audit_folder:
        # Create new folder
        audit_folder = {
            "name": "21. Audit Agent",
            "item": []
        }
        collection['item'].append(audit_folder)
    
    existing_names = {req.get('name') for req in audit_folder.get('item', [])}
    
    # Add missing endpoints
    missing_endpoints = [
        {
            "name": "Get Recent Audit Logs",
            "request": {
                "method": "GET",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{accessToken}}",
                        "type": "text"
                    }
                ],
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/audit/recent?limit=100",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "audit", "recent"],
                    "query": [
                        {
                            "key": "limit",
                            "value": "100",
                            "description": "Number of recent logs to retrieve (default: 100)"
                        }
                    ]
                },
                "description": "Get recent audit logs with optional limit"
            }
        },
        {
            "name": "Search Audit Logs",
            "request": {
                "method": "GET",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{accessToken}}",
                        "type": "text"
                    }
                ],
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/audit/search?keyword={{searchKeyword}}",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "audit", "search"],
                    "query": [
                        {
                            "key": "keyword",
                            "value": "{{searchKeyword}}",
                            "description": "Search keyword"
                        }
                    ]
                },
                "description": "Search audit logs by keyword"
            }
        },
        {
            "name": "Get Audit Statistics",
            "request": {
                "method": "GET",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{accessToken}}",
                        "type": "text"
                    }
                ],
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/audit/statistics",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "audit", "statistics"]
                },
                "description": "Get audit statistics"
            }
        },
        {
            "name": "Cleanup Old Audit Logs",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{accessToken}}",
                        "type": "text"
                    }
                ],
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/audit/cleanup?daysToKeep=90",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "audit", "cleanup"],
                    "query": [
                        {
                            "key": "daysToKeep",
                            "value": "90",
                            "description": "Number of days to keep (logs older than this will be deleted)"
                        }
                    ]
                },
                "description": "Cleanup old audit logs"
            }
        }
    ]
    
    for endpoint in missing_endpoints:
        if endpoint['name'] not in existing_names:
            audit_folder.setdefault('item', []).append(endpoint)

def add_missing_document_endpoints(collection):
    """Add missing Document endpoints"""
    
    # Find Documents folder
    doc_folder = None
    for item in collection['item']:
        if item.get('name') == '13. Documents':
            doc_folder = item
            break
    
    if not doc_folder:
        return
    
    existing_names = {req.get('name') for req in doc_folder.get('item', [])}
    
    # Add missing endpoints
    missing = [
        {
            "name": "Bulk Upload Documents",
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
                    "raw": "{\n  \"userId\": {{userId}},\n  \"username\": \"{{username}}\",\n  \"projectType\": \"{{projectType}}\",\n  \"documents\": [\n    {\n      \"entityType\": \"ASSET\",\n      \"entityId\": {{assetId}},\n      \"docType\": \"PDF\",\n      \"fileName\": \"document1.pdf\"\n    }\n  ]\n}"
                },
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/documents/bulk",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "documents", "bulk"]
                },
                "description": "Bulk upload documents"
            }
        },
        {
            "name": "Bulk Upload Documents (Excel)",
            "request": {
                "method": "POST",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{accessToken}}",
                        "type": "text"
                    }
                ],
                "body": {
                    "mode": "formdata",
                    "formdata": [
                        {
                            "key": "file",
                            "type": "file",
                            "src": []
                        },
                        {
                            "key": "userId",
                            "value": "{{userId}}",
                            "type": "text"
                        },
                        {
                            "key": "username",
                            "value": "{{username}}",
                            "type": "text"
                        },
                        {
                            "key": "projectType",
                            "value": "{{projectType}}",
                            "type": "text"
                        }
                    ]
                },
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/documents/bulk/excel",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "documents", "bulk", "excel"]
                },
                "description": "Bulk upload documents from Excel"
            }
        }
    ]
    
    for endpoint in missing:
        if endpoint['name'] not in existing_names:
            doc_folder.setdefault('item', []).append(endpoint)

if __name__ == "__main__":
    try:
        print("🔧 Fixing and enhancing consolidated collection...")
        
        # Read consolidated collection
        with open('Asset_Service_Consolidated.postman_collection.json', 'r') as f:
            collection = json.load(f)
        
        # Fix variable names
        print("  Fixing variable names (baseUrl -> assetbaseUrl)...")
        fix_variable_names(collection)
        
        # Add missing endpoints
        print("  Adding missing Audit Agent endpoints...")
        add_missing_audit_endpoints(collection)
        
        print("  Adding missing Document endpoints...")
        add_missing_document_endpoints(collection)
        
        # Update collection variables
        for var in collection.get('variable', []):
            if var.get('key') == 'baseUrl':
                var['key'] = 'assetbaseUrl'
                var['value'] = 'http://localhost:8083'
                var['description'] = 'Base URL for Asset Service API'
        
        # Ensure assetbaseUrl variable exists
        var_keys = [v.get('key') for v in collection.get('variable', [])]
        if 'assetbaseUrl' not in var_keys:
            collection.setdefault('variable', []).append({
                "key": "assetbaseUrl",
                "value": "http://localhost:8083",
                "type": "string",
                "description": "Base URL for Asset Service API"
            })
        
        # Write fixed collection
        with open('Asset_Service_Consolidated.postman_collection.json', 'w') as f:
            json.dump(collection, f, indent=2)
        
        print("✅ Collection fixed and enhanced!")
        print(f"   Total folders: {len(collection['item'])}")
        
        # Count total requests
        total_requests = 0
        for folder in collection['item']:
            if 'item' in folder:
                total_requests += len(folder['item'])
        print(f"   Total requests: {total_requests}")
        
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

