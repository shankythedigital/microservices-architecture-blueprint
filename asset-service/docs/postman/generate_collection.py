import json

collection = {
    "info": {
        "_postman_id": "asset-service-complete-api",
        "name": "Asset Service - Complete API Collection",
        "description": "Complete API collection for Asset Management Service with all controllers, environment variables, and body variables",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "item": []
}

def create_request(method, path, name, body=None, description="", is_file_upload=False):
    headers = [
        {"key": "Authorization", "value": "Bearer {{authToken}}", "type": "text"}
    ]
    
    if not is_file_upload:
        headers.append({"key": "Content-Type", "value": "application/json", "type": "text"})
    
    request = {
        "method": method,
        "header": headers,
        "url": {
            "raw": f"{{{{baseUrl}}}}/api/asset/v1/{path}",
            "host": ["{{baseUrl}}"],
            "path": ["api", "asset", "v1"] + path.split("/")
        },
        "description": description
    }
    
    if body:
        if is_file_upload:
            request["body"] = {
                "mode": "formdata",
                "formdata": body
            }
        else:
            request["body"] = {
                "mode": "raw",
                "raw": json.dumps(body, indent=2)
            }
    
    return {"name": name, "request": request}

# 1. Assets
assets_folder = {
    "name": "1. Assets",
    "item": [
        create_request("POST", "assets", "Create Asset", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "asset": {
                "assetNameUdv": "ASSET-{{$randomInt}}",
                "assetStatus": "AVAILABLE",
                "category": {"categoryId": "{{categoryId}}"},
                "subCategory": {"subCategoryId": "{{subCategoryId}}"},
                "make": {"makeId": "{{makeId}}"},
                "model": {"modelId": "{{modelId}}"}
            }
        }, "Create a new asset"),
        create_request("GET", "assets/{{assetId}}", "Get Asset by ID", None, "Get asset details by ID"),
        create_request("PUT", "assets/{{assetId}}", "Update Asset", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "asset": {
                "assetNameUdv": "ASSET-UPDATED",
                "assetStatus": "IN_USE"
            }
        }, "Update an existing asset"),
        create_request("DELETE", "assets/{{assetId}}", "Delete Asset", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete an asset"),
        create_request("GET", "assets/search?keyword={{searchKeyword}}&page=0&size=20", "Search Assets", None, "Search assets with pagination")
    ]
}

# 2. Categories
categories_folder = {
    "name": "2. Categories",
    "item": [
        create_request("POST", "categories", "Create Category", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "category": {
                "categoryName": "Electronics",
                "description": "Electronic devices and equipment"
            }
        }, "Create a new product category"),
        create_request("GET", "categories", "List All Categories", None, "Get all categories"),
        create_request("GET", "categories/{{categoryId}}", "Get Category by ID", None, "Get category by ID"),
        create_request("PUT", "categories/{{categoryId}}", "Update Category", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "category": {
                "categoryName": "Electronics Updated",
                "description": "Updated description"
            }
        }, "Update a category"),
        create_request("DELETE", "categories/{{categoryId}}", "Delete Category", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a category"),
        create_request("POST", "categories/bulk", "Bulk Create Categories", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "categories": [
                {"categoryName": "Category 1", "description": "Desc 1"},
                {"categoryName": "Category 2", "description": "Desc 2"}
            ]
        }, "Create multiple categories"),
        create_request("POST", "categories/bulk/excel", "Bulk Upload Categories (Excel)", None, "Upload Excel file for bulk category creation", True)
    ]
}

# 3. SubCategories
subcategories_folder = {
    "name": "3. SubCategories",
    "item": [
        create_request("POST", "subcategories", "Create SubCategory", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "subCategory": {
                "subCategoryName": "Laptops",
                "description": "Laptop computers",
                "category": {"categoryId": "{{categoryId}}"}
            }
        }, "Create a new subcategory"),
        create_request("GET", "subcategories", "List All SubCategories", None, "Get all subcategories"),
        create_request("GET", "subcategories/{{subCategoryId}}", "Get SubCategory by ID", None, "Get subcategory by ID"),
        create_request("PUT", "subcategories/{{subCategoryId}}", "Update SubCategory", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "subCategory": {
                "subCategoryName": "Laptops Updated",
                "description": "Updated description"
            }
        }, "Update a subcategory"),
        create_request("DELETE", "subcategories/{{subCategoryId}}", "Delete SubCategory", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a subcategory"),
        create_request("POST", "subcategories/bulk", "Bulk Create SubCategories", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "subCategories": [
                {"subCategoryName": "SubCategory 1", "description": "Desc 1", "category": {"categoryId": "{{categoryId}}"}},
                {"subCategoryName": "SubCategory 2", "description": "Desc 2", "category": {"categoryId": "{{categoryId}}"}}
            ]
        }, "Create multiple subcategories"),
        create_request("POST", "subcategories/bulk/excel", "Bulk Upload SubCategories (Excel)", None, "Upload Excel file", True)
    ]
}

# 4. Makes
makes_folder = {
    "name": "4. Makes",
    "item": [
        create_request("POST", "makes", "Create Make", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "make": {
                "makeName": "Dell",
                "subCategory": {"subCategoryId": "{{subCategoryId}}"}
            }
        }, "Create a new make"),
        create_request("GET", "makes", "List All Makes", None, "Get all makes"),
        create_request("GET", "makes/{{makeId}}", "Get Make by ID", None, "Get make by ID"),
        create_request("PUT", "makes/{{makeId}}", "Update Make", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "make": {
                "makeName": "Dell Updated"
            }
        }, "Update a make"),
        create_request("DELETE", "makes/{{makeId}}", "Delete Make", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a make"),
        create_request("POST", "makes/bulk", "Bulk Create Makes", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "makes": [
                {"makeName": "Make 1", "subCategory": {"subCategoryId": "{{subCategoryId}}"}},
                {"makeName": "Make 2", "subCategory": {"subCategoryId": "{{subCategoryId}}"}}
            ]
        }, "Create multiple makes"),
        create_request("POST", "makes/bulk/excel", "Bulk Upload Makes (Excel)", None, "Upload Excel file", True)
    ]
}

# 5. Models
models_folder = {
    "name": "5. Models",
    "item": [
        create_request("POST", "models", "Create Model", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "model": {
                "modelName": "XPS 15",
                "description": "Dell XPS 15 Laptop",
                "make": {"makeId": "{{makeId}}"}
            }
        }, "Create a new model"),
        create_request("GET", "models", "List All Models", None, "Get all models"),
        create_request("GET", "models/{{modelId}}", "Get Model by ID", None, "Get model by ID"),
        create_request("PUT", "models/{{modelId}}", "Update Model", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "model": {
                "modelName": "XPS 15 Updated",
                "description": "Updated description"
            }
        }, "Update a model"),
        create_request("DELETE", "models/{{modelId}}", "Delete Model", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a model"),
        create_request("POST", "models/bulk", "Bulk Create Models", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "models": [
                {"modelName": "Model 1", "make": {"makeId": "{{makeId}}"}},
                {"modelName": "Model 2", "make": {"makeId": "{{makeId}}"}}
            ]
        }, "Create multiple models"),
        create_request("POST", "models/bulk/excel", "Bulk Upload Models (Excel)", None, "Upload Excel file", True)
    ]
}

# 6. Vendors
vendors_folder = {
    "name": "6. Vendors",
    "item": [
        create_request("POST", "vendors", "Create Vendor", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "vendor": {
                "vendorName": "ABC Suppliers",
                "contactPerson": "John Doe",
                "email": "john@abc.com",
                "mobile": "+1234567890",
                "address": "123 Main St"
            }
        }, "Create a new vendor"),
        create_request("GET", "vendors", "List All Vendors", None, "Get all vendors"),
        create_request("GET", "vendors/{{vendorId}}", "Get Vendor by ID", None, "Get vendor by ID"),
        create_request("PUT", "vendors/{{vendorId}}", "Update Vendor", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "vendor": {
                "vendorName": "ABC Suppliers Updated",
                "contactPerson": "Jane Doe",
                "email": "jane@abc.com"
            }
        }, "Update a vendor"),
        create_request("DELETE", "vendors/{{vendorId}}", "Delete Vendor", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a vendor"),
        create_request("POST", "vendors/bulk", "Bulk Create Vendors", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "vendors": [
                {"vendorName": "Vendor 1", "contactPerson": "Person 1", "email": "v1@test.com"},
                {"vendorName": "Vendor 2", "contactPerson": "Person 2", "email": "v2@test.com"}
            ]
        }, "Create multiple vendors"),
        create_request("POST", "vendors/bulk/excel", "Bulk Upload Vendors (Excel)", None, "Upload Excel file", True)
    ]
}

# 7. Outlets
outlets_folder = {
    "name": "7. Outlets",
    "item": [
        create_request("POST", "outlets", "Create Outlet", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "outlet": {
                "outletName": "Main Store",
                "outletAddress": "456 Oak Ave",
                "contactInfo": "+1234567890",
                "vendor": {"vendorId": "{{vendorId}}"}
            }
        }, "Create a new outlet"),
        create_request("GET", "outlets", "List All Outlets", None, "Get all outlets"),
        create_request("GET", "outlets/{{outletId}}", "Get Outlet by ID", None, "Get outlet by ID"),
        create_request("PUT", "outlets/{{outletId}}", "Update Outlet", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "outlet": {
                "outletName": "Main Store Updated",
                "outletAddress": "Updated address"
            }
        }, "Update an outlet"),
        create_request("DELETE", "outlets/{{outletId}}", "Delete Outlet", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete an outlet"),
        create_request("POST", "outlets/bulk", "Bulk Create Outlets", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "outlets": [
                {"outletName": "Outlet 1", "outletAddress": "Address 1", "vendor": {"vendorId": "{{vendorId}}"}},
                {"outletName": "Outlet 2", "outletAddress": "Address 2", "vendor": {"vendorId": "{{vendorId}}"}}
            ]
        }, "Create multiple outlets"),
        create_request("POST", "outlets/bulk/excel", "Bulk Upload Outlets (Excel)", None, "Upload Excel file", True)
    ]
}

# 8. Components
components_folder = {
    "name": "8. Components",
    "item": [
        create_request("POST", "components", "Create Component", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "component": {
                "componentName": "RAM Module",
                "description": "16GB DDR4 RAM"
            }
        }, "Create a new component"),
        create_request("GET", "components", "List All Components", None, "Get all components"),
        create_request("GET", "components/{{componentId}}", "Get Component by ID", None, "Get component by ID"),
        create_request("PUT", "components/{{componentId}}", "Update Component", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "component": {
                "componentName": "RAM Module Updated",
                "description": "32GB DDR4 RAM"
            }
        }, "Update a component"),
        create_request("DELETE", "components/{{componentId}}", "Delete Component", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a component"),
        create_request("POST", "components/bulk", "Bulk Create Components", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "components": [
                {"componentName": "Component 1", "description": "Desc 1"},
                {"componentName": "Component 2", "description": "Desc 2"}
            ]
        }, "Create multiple components"),
        create_request("POST", "components/bulk/excel", "Bulk Upload Components (Excel)", None, "Upload Excel file", True)
    ]
}

# 9. User Links
userlinks_folder = {
    "name": "9. User Links",
    "item": [
        create_request("POST", "userlinks/link", "Link Asset to User", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "link": {
                "asset": {
                    "assetId": "{{assetId}}",
                    "assetuserId": "{{targetUserId}}",
                    "assetusername": "{{targetUsername}}"
                }
            }
        }, "Link an asset to a user"),
        create_request("POST", "userlinks/delink", "Delink Asset from User", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "link": {
                "asset": {
                    "assetId": "{{assetId}}",
                    "assetuserId": "{{targetUserId}}"
                }
            }
        }, "Delink an asset from a user"),
        create_request("POST", "userlinks/link-multiple", "Link Multiple Assets", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "links": [
                {"assetId": "{{assetId}}", "assetuserId": "{{targetUserId}}", "assetusername": "{{targetUsername}}"},
                {"assetId": "{{assetId2}}", "assetuserId": "{{targetUserId}}", "assetusername": "{{targetUsername}}"}
            ]
        }, "Link multiple assets to a user"),
        create_request("POST", "userlinks/delink-multiple", "Delink Multiple Assets", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "links": [
                {"assetId": "{{assetId}}", "assetuserId": "{{targetUserId}}"},
                {"assetId": "{{assetId2}}", "assetuserId": "{{targetUserId}}"}
            ]
        }, "Delink multiple assets from a user"),
        create_request("GET", "userlinks/assigned-assets?userId={{targetUserId}}", "Get Assigned Assets", None, "Get all assets assigned to a user"),
        create_request("GET", "userlinks/asset?assetId={{assetId}}", "Get Asset Link Info", None, "Get link information for an asset"),
        create_request("GET", "userlinks/by-subcategory?subCategoryId={{subCategoryId}}", "Get Users by SubCategory", None, "Get users linked to assets in a subcategory")
    ]
}

# 10. Warranty
warranty_folder = {
    "name": "10. Warranty",
    "item": [
        create_request("POST", "warranty", "Create Warranty", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "warranty": {
                "asset": {"assetId": "{{assetId}}"},
                "warrantyProvider": "Dell Inc",
                "warrantyStartDate": "2024-01-01",
                "warrantyEndDate": "2027-01-01",
                "warrantyTerms": "3 years standard warranty"
            }
        }, "Create warranty for an asset"),
        create_request("GET", "warranty", "List All Warranties", None, "Get all warranties"),
        create_request("GET", "warranty/{{warrantyId}}", "Get Warranty by ID", None, "Get warranty by ID"),
        create_request("PUT", "warranty/{{warrantyId}}", "Update Warranty", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "warranty": {
                "warrantyProvider": "Dell Inc Updated",
                "warrantyTerms": "Extended warranty"
            }
        }, "Update a warranty"),
        create_request("DELETE", "warranty/{{warrantyId}}", "Delete Warranty", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete a warranty")
    ]
}

# 11. AMC
amc_folder = {
    "name": "11. AMC",
    "item": [
        create_request("POST", "amc", "Create AMC", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "amc": {
                "asset": {"assetId": "{{assetId}}"},
                "amcProvider": "Service Provider Inc",
                "amcStartDate": "2024-01-01",
                "amcEndDate": "2025-01-01",
                "amcCost": 5000.00,
                "amcTerms": "Annual maintenance contract"
            }
        }, "Create AMC for an asset"),
        create_request("GET", "amc", "List All AMCs", None, "Get all AMCs"),
        create_request("GET", "amc/{{amcId}}", "Get AMC by ID", None, "Get AMC by ID"),
        create_request("PUT", "amc/{{amcId}}", "Update AMC", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "amc": {
                "amcProvider": "Service Provider Inc Updated",
                "amcCost": 6000.00
            }
        }, "Update an AMC"),
        create_request("DELETE", "amc/{{amcId}}", "Delete AMC", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}"
        }, "Soft delete an AMC")
    ]
}

# 12. Documents
documents_folder = {
    "name": "12. Documents",
    "item": [
        create_request("POST", "documents/upload", "Upload Document", [
            {"key": "file", "type": "file", "src": []},
            {"key": "entityType", "value": "ASSET", "type": "text"},
            {"key": "entityId", "value": "{{assetId}}", "type": "text"},
            {"key": "userId", "value": "{{userId}}", "type": "text"},
            {"key": "username", "value": "{{username}}", "type": "text"},
            {"key": "projectType", "value": "{{projectType}}", "type": "text"},
            {"key": "docType", "value": "PDF", "type": "text"}
        ], "Upload a document for an entity", True),
        create_request("GET", "documents/{{documentId}}", "Get Document by ID", None, "Get document details by ID"),
        create_request("GET", "documents/download/{{documentId}}", "Download Document", None, "Download a document"),
        create_request("DELETE", "documents/{{documentId}}", "Delete Document", {
            "userId": "{{userId}}",
            "username": "{{username}}",
            "projectType": "{{projectType}}",
            "entityType": "ASSET",
            "entityId": "{{assetId}}"
        }, "Soft delete a document")
    ]
}

# 13. Compliance
compliance_folder = {
    "name": "13. Compliance",
    "item": [
        create_request("POST", "compliance/validate", "Validate Entity", {
            "entityType": "{{entityType}}",
            "entityId": "{{entityId}}",
            "autoResolve": False
        }, "Validate an entity against compliance rules"),
        create_request("GET", "compliance/validate/{{entityType}}/{{entityId}}", "Validate Entity by ID", None, "Validate entity by type and ID"),
        create_request("GET", "compliance/status/{{entityType}}/{{entityId}}", "Get Compliance Status", None, "Get compliance status for an entity"),
        create_request("GET", "compliance/violations/{{entityType}}/{{entityId}}", "Get Violations", None, "Get all violations for an entity"),
        create_request("POST", "compliance/violations/{{violationId}}/resolve", "Resolve Violation", {
            "resolvedBy": "{{username}}",
            "notes": "Violation resolved",
            "status": "RESOLVED"
        }, "Resolve a compliance violation"),
        create_request("POST", "compliance/validate/bulk/{{entityType}}", "Bulk Validate", [1, 2, 3], "Validate multiple entities in bulk"),
        create_request("GET", "compliance/metrics", "Get Compliance Metrics", None, "Get overall compliance metrics"),
        create_request("GET", "compliance/metrics/{{entityType}}", "Get Metrics by Entity Type", None, "Get compliance metrics for entity type")
    ]
}

# 14. Compliance Rules
compliance_rules_folder = {
    "name": "14. Compliance Rules",
    "item": [
        create_request("GET", "compliance/rules", "List All Rules", None, "Get all compliance rules"),
        create_request("GET", "compliance/rules/entity-type/{{entityType}}", "Get Rules by Entity Type", None, "Get rules for entity type"),
        create_request("GET", "compliance/rules/{{ruleId}}", "Get Rule by ID", None, "Get rule by ID"),
        create_request("POST", "compliance/rules", "Create Rule", {
            "ruleCode": "ASSET_NAME_REQUIRED",
            "ruleName": "Asset Name Required",
            "description": "Asset must have a name",
            "entityType": "ASSET",
            "ruleType": {"ruleTypeId": 1},
            "severity": {"severityId": 2},
            "ruleExpression": "{\"field\": \"assetNameUdv\", \"operator\": \"notEmpty\"}"
        }, "Create a new compliance rule"),
        create_request("PUT", "compliance/rules/{{ruleId}}", "Update Rule", {
            "ruleName": "Asset Name Required Updated",
            "description": "Updated description"
        }, "Update a compliance rule"),
        create_request("DELETE", "compliance/rules/{{ruleId}}", "Delete Rule", None, "Soft delete a compliance rule")
    ]
}

# 15. Entity Types
entity_types_folder = {
    "name": "15. Entity Types",
    "item": [
        create_request("GET", "entity-types", "List All Entity Types", None, "Get all entity types"),
        create_request("GET", "entity-types/active", "List Active Entity Types", None, "Get active entity types"),
        create_request("GET", "entity-types/code/{{entityTypeCode}}", "Get Entity Type by Code", None, "Get entity type by code"),
        create_request("GET", "entity-types/{{entityTypeId}}", "Get Entity Type by ID", None, "Get entity type by ID"),
        create_request("POST", "entity-types/initialize", "Initialize Entity Types", None, "Initialize default entity types")
    ]
}

# 16. Status
status_folder = {
    "name": "16. Status",
    "item": [
        create_request("GET", "statuses", "List All Statuses", None, "Get all statuses"),
        create_request("GET", "statuses/active", "List Active Statuses", None, "Get active statuses"),
        create_request("GET", "statuses/category/{{statusCategory}}", "Get Statuses by Category", None, "Get statuses by category"),
        create_request("GET", "statuses/code/{{statusCode}}", "Get Status by Code", None, "Get status by code"),
        create_request("GET", "statuses/{{statusId}}", "Get Status by ID", None, "Get status by ID"),
        create_request("POST", "statuses/initialize", "Initialize Statuses", None, "Initialize default statuses")
    ]
}

# 17. Master Data Agent
master_data_folder = {
    "name": "17. Master Data Agent",
    "item": [
        create_request("POST", "masters/categories", "Create Category (Agent)", {
            "categoryName": "Electronics",
            "description": "Electronic devices",
            "createdBy": "{{username}}"
        }, "Create category via master data agent"),
        create_request("POST", "masters/subcategories", "Create SubCategory (Agent)", {
            "subCategoryName": "Laptops",
            "categoryId": "{{categoryId}}",
            "createdBy": "{{username}}"
        }, "Create subcategory via master data agent"),
        create_request("POST", "masters/makes", "Create Make (Agent)", {
            "makeName": "Dell",
            "subCategoryId": "{{subCategoryId}}",
            "createdBy": "{{username}}"
        }, "Create make via master data agent"),
        create_request("POST", "masters/models", "Create Model (Agent)", {
            "modelName": "XPS 15",
            "makeId": "{{makeId}}",
            "createdBy": "{{username}}"
        }, "Create model via master data agent"),
        create_request("POST", "masters/vendors", "Create Vendor (Agent)", {
            "vendorName": "ABC Suppliers",
            "contactPerson": "John Doe",
            "email": "john@abc.com",
            "createdBy": "{{username}}"
        }, "Create vendor via master data agent"),
        create_request("POST", "masters/outlets", "Create Outlet (Agent)", {
            "outletName": "Main Store",
            "outletAddress": "123 Main St",
            "vendorId": "{{vendorId}}",
            "createdBy": "{{username}}"
        }, "Create outlet via master data agent"),
        create_request("GET", "masters/summary", "Get Master Data Summary", None, "Get summary of all master data")
    ]
}

# 18. User Asset Link Agent
user_asset_link_agent_folder = {
    "name": "18. User Asset Link Agent",
    "item": [
        create_request("POST", "user-asset-links/link-asset", "Link Asset", {
            "assetId": "{{assetId}}",
            "userId": "{{targetUserId}}",
            "username": "{{targetUsername}}",
            "createdBy": "{{username}}"
        }, "Link asset to user via agent"),
        create_request("POST", "user-asset-links/link-component", "Link Component", {
            "componentId": "{{componentId}}",
            "userId": "{{targetUserId}}",
            "username": "{{targetUsername}}",
            "createdBy": "{{username}}"
        }, "Link component to user via agent"),
        create_request("GET", "user-asset-links/user/{{targetUserId}}/assets", "Get User Assets", None, "Get all assets linked to a user"),
        create_request("GET", "user-asset-links/asset/{{assetId}}/history", "Get Asset Link History", None, "Get link history for an asset"),
        create_request("POST", "user-asset-links/bulk-link-assets", "Bulk Link Assets", {
            "assetIds": ["{{assetId}}", "{{assetId2}}"],
            "userId": "{{targetUserId}}",
            "username": "{{targetUsername}}",
            "createdBy": "{{username}}"
        }, "Bulk link multiple assets to a user")
    ]
}

# 19. Audit Agent
audit_agent_folder = {
    "name": "19. Audit Agent",
    "item": [
        create_request("POST", "audit/log", "Create Audit Log", {
            "entityType": "ASSET",
            "entityId": "{{assetId}}",
            "action": "CREATE",
            "userId": "{{userId}}",
            "username": "{{username}}",
            "oldValues": {},
            "newValues": {"assetNameUdv": "ASSET-001"}
        }, "Create an audit log entry"),
        create_request("GET", "audit", "List Audit Logs", None, "Get all audit logs"),
        create_request("GET", "audit/username/{{username}}", "Get Logs by Username", None, "Get audit logs by username"),
        create_request("GET", "audit/entity-type/{{entityType}}", "Get Logs by Entity Type", None, "Get audit logs by entity type"),
        create_request("GET", "audit/date-range?startDate=2024-01-01&endDate=2024-12-31", "Get Logs by Date Range", None, "Get audit logs in date range"),
        create_request("GET", "audit/statistics", "Get Audit Statistics", None, "Get audit statistics")
    ]
}

# Add all folders
collection["item"].extend([
    assets_folder,
    categories_folder,
    subcategories_folder,
    makes_folder,
    models_folder,
    vendors_folder,
    outlets_folder,
    components_folder,
    userlinks_folder,
    warranty_folder,
    amc_folder,
    documents_folder,
    compliance_folder,
    compliance_rules_folder,
    entity_types_folder,
    status_folder,
    master_data_folder,
    user_asset_link_agent_folder,
    audit_agent_folder
])

# Write collection
with open("Asset_Service_API.postman_collection.json", "w") as f:
    json.dump(collection, f, indent=2)

print("✅ Postman collection created successfully!")
