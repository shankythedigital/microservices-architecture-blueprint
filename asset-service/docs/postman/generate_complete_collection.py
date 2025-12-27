#!/usr/bin/env python3
"""
Generate comprehensive Postman collection for Asset Service
with detailed environment and body variables.
"""

import json
from datetime import datetime

def create_header():
    """Create standard headers for requests"""
    return [
        {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}",
            "type": "text",
            "description": "JWT Bearer token for authentication. Get from auth-service login endpoint."
        },
        {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
        }
    ]

def create_file_upload_header():
    """Create headers for file upload requests"""
    return [
        {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}",
            "type": "text",
            "description": "JWT Bearer token for authentication"
        }
    ]

def create_request(method, name, path, description="", body=None, is_file_upload=False, query_params=None):
    """Create a Postman request with detailed body variables"""
    url_parts = [p for p in path.split("/") if p]
    
    url_obj = {
        "raw": f"{{{{assetbaseUrl}}}}/api/asset/v1/{path}",
        "host": ["{{assetbaseUrl}}"],
        "path": ["api", "asset", "v1"] + url_parts
    }
    
    if query_params:
        url_obj["query"] = query_params
    
    request_obj = {
        "method": method,
        "header": create_file_upload_header() if is_file_upload else create_header(),
        "url": url_obj,
        "description": description
    }
    
    if body:
        if is_file_upload:
            request_obj["body"] = {
                "mode": "formdata",
                "formdata": body,
                "options": {
                    "raw": {
                        "language": "json"
                    }
                }
            }
        else:
            request_obj["body"] = {
                "mode": "raw",
                "raw": json.dumps(body, indent=2),
                "options": {
                    "raw": {
                        "language": "json"
                    }
                }
            }
    
    return {
        "name": name,
        "request": request_obj,
        "response": []
    }

# ============================================================
# ASSETS
# ============================================================
def create_assets_folder():
    items = [
        create_request("POST", "Create Asset", "assets", 
            "Create a new asset with category, subcategory, make, and model relationships. All foreign keys are required.",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "asset": {
                    "assetNameUdv": "LAPTOP-{{$randomInt}}",
                    "assetStatus": "AVAILABLE",
                    "category": {
                        "categoryId": "{{categoryId}}"
                    },
                    "subCategory": {
                        "subCategoryId": "{{subCategoryId}}"
                    },
                    "make": {
                        "makeId": "{{makeId}}"
                    },
                    "model": {
                        "modelId": "{{modelId}}"
                    }
                }
            }),
        create_request("GET", "Get Asset by ID", "assets/{{assetId}}",
            "Get asset details by ID including all relationships (category, subcategory, make, model)"),
        create_request("PUT", "Update Asset", "assets/{{assetId}}",
            "Update an existing asset. Only provided fields will be updated.",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "asset": {
                    "assetNameUdv": "LAPTOP-UPDATED-{{$randomInt}}",
                    "assetStatus": "IN_USE",
                    "category": {
                        "categoryId": "{{categoryId}}"
                    },
                    "subCategory": {
                        "subCategoryId": "{{subCategoryId}}"
                    },
                    "make": {
                        "makeId": "{{makeId}}"
                    },
                    "model": {
                        "modelId": "{{modelId}}"
                    }
                }
            }),
        create_request("DELETE", "Delete Asset (Soft)", "assets/{{assetId}}",
            "Soft delete an asset. Sets active flag to false.",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}"
            }),
        create_request("GET", "Search Assets", "assets/search",
            "Search assets by keyword with pagination. Searches across asset name, category, make, model.",
            query_params=[
                {"key": "keyword", "value": "{{searchKeyword}}", "description": "Search keyword (searches asset name, category, make, model)"},
                {"key": "page", "value": "0", "description": "Page number (0-indexed)"},
                {"key": "size", "value": "20", "description": "Page size (default: 20)"}
            ]),
        create_request("POST", "Bulk Create Assets", "assets/bulk",
            "Create multiple assets in a single request. Components are stored in separate rows in Excel (one component per row).",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "assets": [
                    {
                        "assetNameUdv": "LAPTOP-001",
                        "assetStatus": "AVAILABLE",
                        "categoryId": "{{categoryId}}",
                        "subCategoryId": "{{subCategoryId}}",
                        "makeId": "{{makeId}}",
                        "modelId": "{{modelId}}",
                        "componentIds": [1, 2]
                    },
                    {
                        "assetNameUdv": "LAPTOP-002",
                        "assetStatus": "IN_USE",
                        "categoryId": "{{categoryId}}",
                        "subCategoryId": "{{subCategoryId}}",
                        "makeId": "{{makeId}}",
                        "modelId": "{{modelId}}",
                        "componentIds": [2, 3]
                    }
                ]
            }),
        create_request("POST", "Bulk Upload Assets (Excel)", "assets/bulk/excel",
            "Upload Excel file for bulk asset creation. Components should be in separate rows (one component per row). Excel format: asset_name_udv, category_id, sub_category_id, make_id, model_id, component_id (one per row).",
            [
                {"key": "file", "type": "file", "src": [], "description": "Excel file (.xlsx) with asset data. Multiple rows per asset for components."},
                {"key": "userId", "value": "{{userId}}", "type": "text", "description": "User ID for audit trail"},
                {"key": "username", "value": "{{username}}", "type": "text", "description": "Username for audit trail"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text", "description": "Project type (default: ASSET_SERVICE)"}
            ],
            is_file_upload=True)
    ]
    return {"name": "1. Assets", "item": items}

# ============================================================
# CATEGORIES
# ============================================================
def create_categories_folder():
    items = [
        create_request("POST", "Create Category", "categories",
            "Create a new product category. Description is required. Category name must be unique (case-insensitive).",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "category": {
                    "categoryName": "Electronics",
                    "description": "Electronic devices and equipment"
                }
            }),
        create_request("GET", "List All Categories", "categories",
            "Get all categories with pagination. Returns active categories only."),
        create_request("GET", "Get Category by ID", "categories/{{categoryId}}",
            "Get category details by ID including description and active status"),
        create_request("PUT", "Update Category", "categories/{{categoryId}}",
            "Update an existing category. Description is required.",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "category": {
                    "categoryName": "Electronics Updated",
                    "description": "Updated description for electronics category"
                }
            }),
        create_request("DELETE", "Delete Category (Soft)", "categories/{{categoryId}}",
            "Soft delete a category. Sets active flag to false.",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}"
            }),
        create_request("POST", "Bulk Create Categories", "categories/bulk",
            "Create multiple categories in a single request. Returns success/failure count for each item.",
            {
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "categories": [
                    {
                        "categoryName": "Category 1",
                        "description": "Description for Category 1"
                    },
                    {
                        "categoryName": "Category 2",
                        "description": "Description for Category 2"
                    }
                ]
            }),
        create_request("POST", "Bulk Upload Categories (Excel)", "categories/bulk/excel",
            "Upload Excel file for bulk category creation. Excel format: category_name (required), description (required). Column names are case-insensitive.",
            [
                {"key": "file", "type": "file", "src": [], "description": "Excel file (.xlsx) with category data. Columns: category_name, description"},
                {"key": "userId", "value": "{{userId}}", "type": "text", "description": "User ID"},
                {"key": "username", "value": "{{username}}", "type": "text", "description": "Username"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text", "description": "Project type"}
            ],
            is_file_upload=True)
    ]
    return {"name": "2. Categories", "item": items}

# Continue with other folders - I'll create a comprehensive script
# Due to size, I'll write the complete collection directly

print("Generating comprehensive Postman collection...")

