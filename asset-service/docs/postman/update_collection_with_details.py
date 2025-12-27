#!/usr/bin/env python3
"""
Update Postman collection with detailed environment and body variables
based on all controllers in asset-service.
"""

import json
import sys

def update_environment_file():
    """Update environment file with comprehensive variables"""
    env_file = "asset-service/docs/postman/Asset_Service_Environment.postman_environment.json"
    
    try:
        with open(env_file, 'r') as f:
            env = json.load(f)
    except FileNotFoundError:
        env = {
            "id": "asset-service-env",
            "name": "Asset Service - Local",
            "values": [],
            "_postman_variable_scope": "environment"
        }
    
    # Define all environment variables with descriptions
    variables = {
        "assetbaseUrl": {
            "value": "http://localhost:8083",
            "type": "default",
            "description": "Base URL for Asset Service API"
        },
        "accessToken": {
            "value": "",
            "type": "secret",
            "description": "JWT Bearer token from auth-service login endpoint"
        },
        "userId": {
            "value": "1",
            "type": "default",
            "description": "Current user ID for operations"
        },
        "username": {
            "value": "admin",
            "type": "default",
            "description": "Current username for operations"
        },
        "projectType": {
            "value": "ASSET_SERVICE",
            "type": "default",
            "description": "Project type for notifications and audit"
        },
        "assetId": {
            "value": "1",
            "type": "default",
            "description": "Asset ID for operations"
        },
        "categoryId": {
            "value": "1",
            "type": "default",
            "description": "Category ID for operations"
        },
        "subCategoryId": {
            "value": "1",
            "type": "default",
            "description": "SubCategory ID for operations"
        },
        "makeId": {
            "value": "1",
            "type": "default",
            "description": "Make ID for operations"
        },
        "modelId": {
            "value": "1",
            "type": "default",
            "description": "Model ID for operations"
        },
        "vendorId": {
            "value": "1",
            "type": "default",
            "description": "Vendor ID for operations"
        },
        "outletId": {
            "value": "1",
            "type": "default",
            "description": "Outlet ID for operations"
        },
        "componentId": {
            "value": "1",
            "type": "default",
            "description": "Component ID for operations"
        },
        "warrantyId": {
            "value": "1",
            "type": "default",
            "description": "Warranty ID for operations"
        },
        "amcId": {
            "value": "1",
            "type": "default",
            "description": "AMC ID for operations"
        },
        "documentId": {
            "value": "1",
            "type": "default",
            "description": "Document ID for operations"
        },
        "targetUserId": {
            "value": "2",
            "type": "default",
            "description": "Target user ID for linking operations"
        },
        "targetUsername": {
            "value": "user1",
            "type": "default",
            "description": "Target username for linking operations"
        },
        "entityType": {
            "value": "ASSET",
            "type": "default",
            "description": "Entity type for compliance/document operations (ASSET, CATEGORY, VENDOR, etc.)"
        },
        "entityId": {
            "value": "1",
            "type": "default",
            "description": "Entity ID for compliance/document operations"
        },
        "searchKeyword": {
            "value": "laptop",
            "type": "default",
            "description": "Search keyword for asset search"
        },
        "filename": {
            "value": "example.pdf",
            "type": "default",
            "description": "File name for download operations"
        },
        "ruleId": {
            "value": "1",
            "type": "default",
            "description": "Compliance rule ID"
        },
        "violationId": {
            "value": "1",
            "type": "default",
            "description": "Compliance violation ID"
        }
    }
    
    # Update environment values
    existing_keys = {v["key"]: i for i, v in enumerate(env.get("values", []))}
    
    for key, config in variables.items():
        var_obj = {
            "key": key,
            "value": config["value"],
            "type": config["type"],
            "enabled": True
        }
        if "description" in config:
            var_obj["description"] = config["description"]
        
        if key in existing_keys:
            env["values"][existing_keys[key]] = var_obj
        else:
            env["values"].append(var_obj)
    
    with open(env_file, 'w') as f:
        json.dump(env, f, indent=2)
    
    print(f"✅ Environment file updated: {env_file}")

def enhance_request_body(body_str, endpoint_name):
    """Enhance request body with detailed variable descriptions"""
    if not body_str or body_str.strip() == "":
        return body_str
    
    try:
        body_obj = json.loads(body_str)
    except:
        return body_str
    
    # Add comments/descriptions based on endpoint
    # This is a simplified version - in practice, you'd want more sophisticated logic
    return json.dumps(body_obj, indent=2)

if __name__ == "__main__":
    update_environment_file()
    print("✅ Environment file updated successfully!")
    print("\n📝 Next steps:")
    print("   1. Import the updated environment file into Postman")
    print("   2. Set the accessToken after logging in via auth-service")
    print("   3. Update entity IDs (categoryId, assetId, etc.) as needed")

