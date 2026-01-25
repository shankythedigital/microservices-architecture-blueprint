#!/usr/bin/env python3
"""
Script to generate complete Postman collections for asset-service and helpdesk-service
"""

import json
from datetime import datetime

def create_asset_service_collection():
    """Create complete Postman collection for Asset Service"""
    
    collection = {
        "info": {
            "_postman_id": "asset-service-complete-v1",
            "name": "Asset Service - Complete API Collection",
            "description": "Complete Postman collection for Asset Management Service with all controllers and endpoints",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "_exporter_id": "asset-service"
        },
        "item": [],
        "variable": [
            {"key": "assetbaseUrl", "value": "http://localhost:8083", "type": "string"},
            {"key": "bearerToken", "value": "your-jwt-token-here", "type": "string"},
            {"key": "userId", "value": "1", "type": "string"},
            {"key": "username", "value": "admin", "type": "string"},
            {"key": "projectType", "value": "ASSET_SERVICE", "type": "string"}
        ]
    }
    
    # Helper function to create request
    def create_request(name, method, path, description="", body=None, params=None, headers=None):
        if headers is None:
            headers = [
                {
                    "key": "Authorization",
                    "value": "Bearer {{bearerToken}}",
                    "type": "text"
                },
                {
                    "key": "Content-Type",
                    "value": "application/json",
                    "type": "text"
                }
            ]
        
        url_parts = path.split("/")
        url_path = []
        url_vars = []
        
        for part in url_parts:
            if part.startswith(":"):
                var_name = part[1:]
                url_path.append(part)
                url_vars.append({
                    "key": var_name,
                    "value": "1",
                    "description": f"{var_name.replace('Id', ' ID')}"
                })
            elif part:
                url_path.append(part)
        
        request_obj = {
            "name": name,
            "request": {
                "method": method,
                "header": headers,
                "url": {
                    "raw": "{{assetbaseUrl}}" + "/" + "/".join(url_path),
                    "host": ["{{assetbaseUrl}}"],
                    "path": url_path
                },
                "description": description
            },
            "response": []
        }
        
        if url_vars:
            request_obj["request"]["url"]["variable"] = url_vars
        
        if body:
            request_obj["request"]["body"] = body
        
        if params:
            request_obj["request"]["url"]["query"] = params
        
        return request_obj
    
    # 1. ASSETS
    assets_folder = {
        "name": "1. Assets",
        "item": [
            create_request(
                "Create Asset",
                "POST",
                "/api/asset/v1/assets",
                "Create a new asset",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "asset": {
                            "assetNameUdv": "Laptop Dell XPS 15",
                            "modelId": 1,
                            "serialNumber": "SN123456",
                            "categoryId": 1,
                            "subCategoryId": 1,
                            "makeId": 1,
                            "assetStatus": "ACTIVE"
                        }
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Update Asset",
                "PUT",
                "/api/asset/v1/assets/:id",
                "Update an existing asset",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "asset": {
                            "assetNameUdv": "Laptop Dell XPS 15 Updated",
                            "assetStatus": "ACTIVE"
                        }
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Delete Asset (Soft Delete)",
                "DELETE",
                "/api/asset/v1/assets/:id",
                "Soft delete an asset",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get Asset by ID",
                "GET",
                "/api/asset/v1/assets/:id",
                "Get asset details by ID"
            ),
            create_request(
                "Search Assets",
                "GET",
                "/api/asset/v1/assets/search",
                "Search assets with pagination",
                params=[
                    {"key": "keyword", "value": "laptop", "description": "Search keyword"},
                    {"key": "page", "value": "0", "description": "Page number"},
                    {"key": "size", "value": "20", "description": "Page size"}
                ]
            ),
            create_request(
                "Bulk Upload Assets",
                "POST",
                "/api/asset/v1/assets/bulk",
                "Bulk create multiple assets",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "assets": [
                            {
                                "assetNameUdv": "Asset 1",
                                "modelId": 1,
                                "serialNumber": "SN001"
                            },
                            {
                                "assetNameUdv": "Asset 2",
                                "modelId": 2,
                                "serialNumber": "SN002"
                            }
                        ]
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Bulk Upload Assets from Excel",
                "POST",
                "/api/asset/v1/assets/bulk/excel",
                "Bulk upload assets from Excel file",
                {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": []},
                        {"key": "userId", "value": "{{userId}}", "type": "text"},
                        {"key": "username", "value": "{{username}}", "type": "text"},
                        {"key": "projectType", "value": "{{projectType}}", "type": "text"}
                    ]
                },
                headers=[
                    {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}
                ]
            ),
            create_request(
                "Complete Asset Creation",
                "POST",
                "/api/asset/v1/assets/complete",
                "Create asset with warranty, document, and user assignment in one request",
                {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "userId", "value": "{{userId}}", "type": "text"},
                        {"key": "username", "value": "{{username}}", "type": "text"},
                        {"key": "projectType", "value": "{{projectType}}", "type": "text"},
                        {"key": "assetNameUdv", "value": "Complete Asset", "type": "text"},
                        {"key": "modelId", "value": "1", "type": "text"},
                        {"key": "serialNumber", "value": "SN123", "type": "text"},
                        {"key": "warrantyStartDate", "value": "2024-01-01", "type": "text"},
                        {"key": "warrantyEndDate", "value": "2025-01-01", "type": "text"},
                        {"key": "targetUserId", "value": "1", "type": "text"},
                        {"key": "purchaseInvoice", "type": "file", "src": []}
                    ]
                },
                headers=[
                    {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}
                ]
            ),
            create_request(
                "Update Asset Favourite",
                "PUT",
                "/api/asset/v1/assets/:id/favourite",
                "Toggle favourite status for an asset",
                params=[{"key": "isFavourite", "value": "true", "description": "Favourite status"}]
            ),
            create_request(
                "Update Asset Most Like",
                "PUT",
                "/api/asset/v1/assets/:id/most-like",
                "Toggle most like status for an asset",
                params=[{"key": "isMostLike", "value": "true", "description": "Most like status"}]
            ),
            create_request(
                "Update Asset Sequence Order",
                "PUT",
                "/api/asset/v1/assets/:id/sequence-order",
                "Update sequence order for an asset (admin only)",
                params=[{"key": "sequenceOrder", "value": "1", "description": "Sequence order"}]
            )
        ]
    }
    collection["item"].append(assets_folder)
    
    # 2. CATEGORIES
    categories_folder = {
        "name": "2. Categories",
        "item": [
            create_request(
                "Create Category",
                "POST",
                "/api/asset/v1/categories",
                "Create a new category",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "category": {
                            "categoryName": "Electronics",
                            "description": "Electronic devices"
                        }
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Update Category",
                "PUT",
                "/api/asset/v1/categories/:id",
                "Update a category",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "category": {
                            "categoryName": "Electronics Updated"
                        }
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Delete Category",
                "DELETE",
                "/api/asset/v1/categories/:id",
                "Soft delete a category",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "List All Categories",
                "GET",
                "/api/asset/v1/categories",
                "Get all categories"
            ),
            create_request(
                "Get Category by ID",
                "GET",
                "/api/asset/v1/categories/:id",
                "Get category by ID"
            ),
            create_request(
                "Bulk Upload Categories",
                "POST",
                "/api/asset/v1/categories/bulk",
                "Bulk create categories",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "categories": [
                            {"categoryName": "Category 1"},
                            {"categoryName": "Category 2"}
                        ]
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Bulk Upload Categories from Excel",
                "POST",
                "/api/asset/v1/categories/bulk/excel",
                "Bulk upload categories from Excel",
                {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": []},
                        {"key": "userId", "value": "{{userId}}", "type": "text"},
                        {"key": "username", "value": "{{username}}", "type": "text"},
                        {"key": "projectType", "value": "{{projectType}}", "type": "text"}
                    ]
                },
                headers=[
                    {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}
                ]
            ),
            create_request(
                "Update Category Favourite",
                "PUT",
                "/api/asset/v1/categories/:id/favourite",
                "Toggle favourite status",
                params=[{"key": "isFavourite", "value": "true"}]
            ),
            create_request(
                "Update Category Most Like",
                "PUT",
                "/api/asset/v1/categories/:id/most-like",
                "Toggle most like status",
                params=[{"key": "isMostLike", "value": "true"}]
            ),
            create_request(
                "Update Category Sequence Order",
                "PUT",
                "/api/asset/v1/categories/:id/sequence-order",
                "Update sequence order",
                params=[{"key": "sequenceOrder", "value": "1"}]
            )
        ]
    }
    collection["item"].append(categories_folder)
    
    # Continue with other controllers... (SubCategories, Makes, Models, Components, etc.)
    # For brevity, I'll add key ones and you can expand
    
    # 3. DOCUMENTS
    documents_folder = {
        "name": "3. Documents",
        "item": [
            create_request(
                "Upload Document",
                "POST",
                "/api/asset/v1/documents/upload",
                "Upload a single document file",
                {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": []},
                        {"key": "entityType", "value": "ASSET", "type": "text"},
                        {"key": "entityId", "value": "1", "type": "text"},
                        {"key": "userId", "value": "{{userId}}", "type": "text"},
                        {"key": "username", "value": "{{username}}", "type": "text"},
                        {"key": "projectType", "value": "{{projectType}}", "type": "text"},
                        {"key": "docType", "value": "PDF", "type": "text"}
                    ]
                },
                headers=[
                    {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}
                ]
            ),
            create_request(
                "Get Document Details",
                "GET",
                "/api/asset/v1/documents/:id",
                "Get document details by ID"
            ),
            create_request(
                "Download Document",
                "GET",
                "/api/asset/v1/documents/download/:id",
                "Download the actual document file"
            ),
            create_request(
                "Delete Document",
                "DELETE",
                "/api/asset/v1/documents/:id",
                "Soft delete a document",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "entityType": "ASSET",
                        "entityId": 1
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Bulk Upload Documents",
                "POST",
                "/api/asset/v1/documents/bulk",
                "Bulk upload multiple documents with files",
                {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "files", "type": "file", "src": []},
                        {"key": "files", "type": "file", "src": []},
                        {"key": "request", "value": json.dumps({
                            "documents": [
                                {
                                    "entityType": "ASSET",
                                    "entityId": 1,
                                    "fileName": "doc1.pdf",
                                    "docType": "PDF"
                                },
                                {
                                    "entityType": "ASSET",
                                    "entityId": 2,
                                    "fileName": "doc2.jpg",
                                    "docType": "IMAGE"
                                }
                            ]
                        }), "type": "text"},
                        {"key": "userId", "value": "{{userId}}", "type": "text"},
                        {"key": "username", "value": "{{username}}", "type": "text"},
                        {"key": "projectType", "value": "{{projectType}}", "type": "text"}
                    ]
                },
                headers=[
                    {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}
                ]
            ),
            create_request(
                "Bulk Upload Documents (File Paths)",
                "POST",
                "/api/asset/v1/documents/bulk/paths",
                "Bulk upload documents using existing file paths",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "userId": "{{userId}}",
                        "username": "{{username}}",
                        "projectType": "{{projectType}}",
                        "documents": [
                            {
                                "entityType": "ASSET",
                                "entityId": 1,
                                "fileName": "document1.pdf",
                                "filePath": "/path/to/file/document1.pdf",
                                "docType": "PDF"
                            }
                        ]
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Bulk Upload Documents from Excel",
                "POST",
                "/api/asset/v1/documents/bulk/excel",
                "Bulk upload documents from Excel file",
                {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": []},
                        {"key": "userId", "value": "{{userId}}", "type": "text"},
                        {"key": "username", "value": "{{username}}", "type": "text"},
                        {"key": "projectType", "value": "{{projectType}}", "type": "text"}
                    ]
                },
                headers=[
                    {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}
                ]
            )
        ]
    }
    collection["item"].append(documents_folder)
    
    # 4. SUBCATEGORIES
    subcategories_folder = {
        "name": "4. SubCategories",
        "item": [
            create_request("Create SubCategory", "POST", "/api/asset/v1/subcategories", "Create a new subcategory",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "subCategory": {"subCategoryName": "Laptops", "categoryId": 1}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update SubCategory", "PUT", "/api/asset/v1/subcategories/:id", "Update a subcategory",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "subCategory": {"subCategoryName": "Laptops Updated"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete SubCategory", "DELETE", "/api/asset/v1/subcategories/:id", "Soft delete a subcategory",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All SubCategories", "GET", "/api/asset/v1/subcategories", "Get all subcategories"),
            create_request("Get SubCategory by ID", "GET", "/api/asset/v1/subcategories/:id", "Get subcategory by ID"),
            create_request("Bulk Upload SubCategories", "POST", "/api/asset/v1/subcategories/bulk", "Bulk create subcategories",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "subCategories": [{"subCategoryName": "SubCat 1", "categoryId": 1}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Bulk Upload SubCategories from Excel", "POST", "/api/asset/v1/subcategories/bulk/excel", "Bulk upload from Excel",
                {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}, {"key": "userId", "value": "{{userId}}", "type": "text"}, {"key": "username", "value": "{{username}}", "type": "text"}, {"key": "projectType", "value": "{{projectType}}", "type": "text"}]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update SubCategory Favourite", "PUT", "/api/asset/v1/subcategories/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update SubCategory Most Like", "PUT", "/api/asset/v1/subcategories/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update SubCategory Sequence Order", "PUT", "/api/asset/v1/subcategories/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(subcategories_folder)
    
    # 5. MAKES
    makes_folder = {
        "name": "5. Makes",
        "item": [
            create_request("Create Make", "POST", "/api/asset/v1/makes", "Create a new make",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "make": {"makeName": "Dell", "subCategoryId": 1}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Make", "PUT", "/api/asset/v1/makes/:id", "Update a make",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "make": {"makeName": "Dell Updated"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Make", "DELETE", "/api/asset/v1/makes/:id", "Soft delete a make",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All Makes", "GET", "/api/asset/v1/makes", "Get all makes"),
            create_request("Get Make by ID", "GET", "/api/asset/v1/makes/:id", "Get make by ID"),
            create_request("Bulk Upload Makes", "POST", "/api/asset/v1/makes/bulk", "Bulk create makes",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "makes": [{"makeName": "Make 1", "subCategoryId": 1}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Bulk Upload Makes from Excel", "POST", "/api/asset/v1/makes/bulk/excel", "Bulk upload from Excel",
                {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}, {"key": "userId", "value": "{{userId}}", "type": "text"}, {"key": "username", "value": "{{username}}", "type": "text"}, {"key": "projectType", "value": "{{projectType}}", "type": "text"}]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update Make Favourite", "PUT", "/api/asset/v1/makes/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Make Most Like", "PUT", "/api/asset/v1/makes/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Make Sequence Order", "PUT", "/api/asset/v1/makes/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(makes_folder)
    
    # 6. MODELS
    models_folder = {
        "name": "6. Models",
        "item": [
            create_request("Create Model", "POST", "/api/asset/v1/models", "Create a new model",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "model": {"modelName": "XPS 15", "makeId": 1}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Model", "PUT", "/api/asset/v1/models/:id", "Update a model",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "model": {"modelName": "XPS 15 Updated"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Model", "DELETE", "/api/asset/v1/models/:id", "Soft delete a model",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All Models", "GET", "/api/asset/v1/models", "Get all models"),
            create_request("Get Model by ID", "GET", "/api/asset/v1/models/:id", "Get model by ID"),
            create_request("Bulk Upload Models", "POST", "/api/asset/v1/models/bulk", "Bulk create models",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "models": [{"modelName": "Model 1", "makeId": 1}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Bulk Upload Models from Excel", "POST", "/api/asset/v1/models/bulk/excel", "Bulk upload from Excel",
                {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}, {"key": "userId", "value": "{{userId}}", "type": "text"}, {"key": "username", "value": "{{username}}", "type": "text"}, {"key": "projectType", "value": "{{projectType}}", "type": "text"}]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update Model Favourite", "PUT", "/api/asset/v1/models/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Model Most Like", "PUT", "/api/asset/v1/models/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Model Sequence Order", "PUT", "/api/asset/v1/models/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(models_folder)
    
    # 7. COMPONENTS
    components_folder = {
        "name": "7. Components",
        "item": [
            create_request("Create Component", "POST", "/api/asset/v1/components", "Create a new component",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "component": {"componentName": "RAM 16GB", "description": "16GB DDR4 RAM"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Component", "PUT", "/api/asset/v1/components/:id", "Update a component",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "component": {"componentName": "RAM 32GB"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Component", "DELETE", "/api/asset/v1/components/:id", "Soft delete a component",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All Components", "GET", "/api/asset/v1/components", "Get all components"),
            create_request("Get Component by ID", "GET", "/api/asset/v1/components/:id", "Get component by ID"),
            create_request("Bulk Upload Components", "POST", "/api/asset/v1/components/bulk", "Bulk create components",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "components": [{"componentName": "Component 1"}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Bulk Upload Components from Excel", "POST", "/api/asset/v1/components/bulk/excel", "Bulk upload from Excel",
                {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}, {"key": "userId", "value": "{{userId}}", "type": "text"}, {"key": "username", "value": "{{username}}", "type": "text"}, {"key": "projectType", "value": "{{projectType}}", "type": "text"}]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update Component Favourite", "PUT", "/api/asset/v1/components/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Component Most Like", "PUT", "/api/asset/v1/components/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Component Sequence Order", "PUT", "/api/asset/v1/components/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(components_folder)
    
    # 8. OUTLETS
    outlets_folder = {
        "name": "8. Outlets",
        "item": [
            create_request("Create Outlet", "POST", "/api/asset/v1/outlets", "Create a new outlet",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "outlet": {"outletName": "Best Buy", "address": "123 Main St"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Outlet", "PUT", "/api/asset/v1/outlets/:id", "Update an outlet",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "outlet": {"outletName": "Best Buy Updated"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Outlet", "DELETE", "/api/asset/v1/outlets/:id", "Soft delete an outlet",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All Outlets", "GET", "/api/asset/v1/outlets", "Get all outlets"),
            create_request("Get Outlet by ID", "GET", "/api/asset/v1/outlets/:id", "Get outlet by ID"),
            create_request("Bulk Upload Outlets", "POST", "/api/asset/v1/outlets/bulk", "Bulk create outlets",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "outlets": [{"outletName": "Outlet 1"}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Bulk Upload Outlets from Excel", "POST", "/api/asset/v1/outlets/bulk/excel", "Bulk upload from Excel",
                {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}, {"key": "userId", "value": "{{userId}}", "type": "text"}, {"key": "username", "value": "{{username}}", "type": "text"}, {"key": "projectType", "value": "{{projectType}}", "type": "text"}]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update Outlet Favourite", "PUT", "/api/asset/v1/outlets/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Outlet Most Like", "PUT", "/api/asset/v1/outlets/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Outlet Sequence Order", "PUT", "/api/asset/v1/outlets/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(outlets_folder)
    
    # 9. VENDORS
    vendors_folder = {
        "name": "9. Vendors",
        "item": [
            create_request("Create Vendor", "POST", "/api/asset/v1/vendors", "Create a new vendor",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "vendor": {"vendorName": "Dell Inc", "contactEmail": "contact@dell.com"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Vendor", "PUT", "/api/asset/v1/vendors/:id", "Update a vendor",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "vendor": {"vendorName": "Dell Inc Updated"}}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Vendor", "DELETE", "/api/asset/v1/vendors/:id", "Soft delete a vendor",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All Vendors", "GET", "/api/asset/v1/vendors", "Get all vendors"),
            create_request("Get Vendor by ID", "GET", "/api/asset/v1/vendors/:id", "Get vendor by ID"),
            create_request("Bulk Upload Vendors", "POST", "/api/asset/v1/vendors/bulk", "Bulk create vendors",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "vendors": [{"vendorName": "Vendor 1"}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Bulk Upload Vendors from Excel", "POST", "/api/asset/v1/vendors/bulk/excel", "Bulk upload from Excel",
                {"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}, {"key": "userId", "value": "{{userId}}", "type": "text"}, {"key": "username", "value": "{{username}}", "type": "text"}, {"key": "projectType", "value": "{{projectType}}", "type": "text"}]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update Vendor Favourite", "PUT", "/api/asset/v1/vendors/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Vendor Most Like", "PUT", "/api/asset/v1/vendors/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Vendor Sequence Order", "PUT", "/api/asset/v1/vendors/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(vendors_folder)
    
    # 10. WARRANTY
    warranty_folder = {
        "name": "10. Warranty",
        "item": [
            create_request("Create Warranty", "POST", "/api/asset/v1/warranty", "Create a new warranty",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "warrantyStartDate": "2024-01-01", "warrantyEndDate": "2025-01-01", "warrantyProvider": "Dell", "warrantyStatus": "ACTIVE"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Warranty", "PUT", "/api/asset/v1/warranty/:id", "Update a warranty",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "warrantyStatus": "EXPIRED"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Warranty", "DELETE", "/api/asset/v1/warranty/:id", "Soft delete a warranty",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All Warranties", "GET", "/api/asset/v1/warranty", "Get all warranties"),
            create_request("Get Warranty by ID", "GET", "/api/asset/v1/warranty/:id", "Get warranty by ID"),
            create_request("Update Warranty Favourite", "PUT", "/api/asset/v1/warranty/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Warranty Most Like", "PUT", "/api/asset/v1/warranty/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Warranty Sequence Order", "PUT", "/api/asset/v1/warranty/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(warranty_folder)
    
    # 11. AMC
    amc_folder = {
        "name": "11. AMC",
        "item": [
            create_request("Create AMC", "POST", "/api/asset/v1/amc", "Create a new AMC",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "amcStartDate": "2024-01-01", "amcEndDate": "2025-01-01", "amcProvider": "Dell", "amcStatus": "ACTIVE"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update AMC", "PUT", "/api/asset/v1/amc/:id", "Update an AMC",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "amcStatus": "EXPIRED"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete AMC", "DELETE", "/api/asset/v1/amc/:id", "Soft delete an AMC",
                {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("List All AMCs", "GET", "/api/asset/v1/amc", "Get all AMCs"),
            create_request("Get AMC by ID", "GET", "/api/asset/v1/amc/:id", "Get AMC by ID"),
            create_request("Update AMC Favourite", "PUT", "/api/asset/v1/amc/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update AMC Most Like", "PUT", "/api/asset/v1/amc/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update AMC Sequence Order", "PUT", "/api/asset/v1/amc/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(amc_folder)
    
    # 12. USER LINKS
    userlinks_folder = {
        "name": "12. User Links",
        "item": [
            create_request("Link Entity to User", "POST", "/api/asset/v1/userlinks/link", "Link an entity (ASSET, COMPONENT, etc.) to a user",
                {"mode": "raw", "raw": json.dumps({"entityType": "ASSET", "entityId": 1, "targetUserId": 1, "targetUsername": "user1", "userId": "{{userId}}", "username": "{{username}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delink Entity from User", "POST", "/api/asset/v1/userlinks/delink", "Delink an entity from a user",
                {"mode": "raw", "raw": json.dumps({"entityType": "ASSET", "entityId": 1, "targetUserId": 1, "targetUsername": "user1", "userId": "{{userId}}", "username": "{{username}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Link Multiple Entities", "POST", "/api/asset/v1/userlinks/link-multiple", "Link multiple entities to a user in one request",
                {"mode": "raw", "raw": json.dumps({"targetUserId": 1, "targetUsername": "user1", "userId": "{{userId}}", "username": "{{username}}", "entities": [{"entityType": "ASSET", "entityId": 1}, {"entityType": "COMPONENT", "entityId": 1}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delink Multiple Entities", "POST", "/api/asset/v1/userlinks/delink-multiple", "Delink multiple entities from a user",
                {"mode": "raw", "raw": json.dumps({"targetUserId": 1, "targetUsername": "user1", "userId": "{{userId}}", "username": "{{username}}", "entities": [{"entityType": "ASSET", "entityId": 1}]}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Get Assigned Assets", "GET", "/api/asset/v1/userlinks/assigned-assets", "Get all assets assigned to a user",
                params=[{"key": "targetUserId", "value": "1"}]),
            create_request("Get Single Asset", "GET", "/api/asset/v1/userlinks/asset", "Get single asset details",
                params=[{"key": "assetId", "value": "1"}]),
            create_request("Get Users by SubCategory", "GET", "/api/asset/v1/userlinks/by-subcategory", "Get users by subcategory",
                params=[{"key": "subCategoryId", "value": "1"}]),
            create_request("Get All Master Data in Detail", "GET", "/api/asset/v1/userlinks/master-data/all", "Get comprehensive master data",
                params=[{"key": "userId", "value": "1", "disabled": True}]),
            create_request("Get Need Your Attention", "GET", "/api/asset/v1/userlinks/need-your-attention", "Get comprehensive attention data for logged-in user")
        ]
    }
    collection["item"].append(userlinks_folder)
    
    # 13. STATUSES
    statuses_folder = {
        "name": "13. Statuses",
        "item": [
            create_request("List All Statuses", "GET", "/api/asset/v1/statuses", "Get all statuses"),
            create_request("List Active Statuses", "GET", "/api/asset/v1/statuses/active", "Get active statuses"),
            create_request("List Statuses by Category", "GET", "/api/asset/v1/statuses/category/:category", "Get statuses by category"),
            create_request("List Active Statuses by Category", "GET", "/api/asset/v1/statuses/category/:category/active", "Get active statuses by category"),
            create_request("Find Status by Code", "GET", "/api/asset/v1/statuses/code/:code", "Find status by code"),
            create_request("Find Status by ID", "GET", "/api/asset/v1/statuses/:id", "Find status by ID"),
            create_request("Validate Status", "GET", "/api/asset/v1/statuses/validate/:code", "Validate status code"),
            create_request("Initialize Statuses", "POST", "/api/asset/v1/statuses/initialize", "Initialize default statuses"),
            create_request("Update Status Favourite", "PUT", "/api/asset/v1/statuses/:id/favourite", "Toggle favourite", params=[{"key": "isFavourite", "value": "true"}]),
            create_request("Update Status Most Like", "PUT", "/api/asset/v1/statuses/:id/most-like", "Toggle most like", params=[{"key": "isMostLike", "value": "true"}]),
            create_request("Update Status Sequence Order", "PUT", "/api/asset/v1/statuses/:id/sequence-order", "Update sequence order", params=[{"key": "sequenceOrder", "value": "1"}])
        ]
    }
    collection["item"].append(statuses_folder)
    
    # 14. ENTITY TYPES
    entitytypes_folder = {
        "name": "14. Entity Types",
        "item": [
            create_request("List All Entity Types", "GET", "/api/asset/v1/entity-types", "Get all entity types"),
            create_request("List Active Entity Types", "GET", "/api/asset/v1/entity-types/active", "Get active entity types"),
            create_request("Find Entity Type by Code", "GET", "/api/asset/v1/entity-types/code/:code", "Find entity type by code"),
            create_request("Find Entity Type by ID", "GET", "/api/asset/v1/entity-types/:id", "Find entity type by ID"),
            create_request("Validate Entity Type", "GET", "/api/asset/v1/entity-types/validate/:code", "Validate entity type code"),
            create_request("Initialize Entity Types", "POST", "/api/asset/v1/entity-types/initialize", "Initialize default entity types")
        ]
    }
    collection["item"].append(entitytypes_folder)
    
    # 15. COMPLIANCE
    compliance_folder = {
        "name": "15. Compliance",
        "item": [
            create_request("Validate Entity", "POST", "/api/asset/v1/compliance/validate", "Validate entity compliance",
                {"mode": "raw", "raw": json.dumps({"entityType": "ASSET", "entityId": 1}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Validate Entity by Type and ID", "GET", "/api/asset/v1/compliance/validate/:entityType/:entityId", "Validate entity compliance"),
            create_request("Get Compliance Status", "GET", "/api/asset/v1/compliance/status/:entityType/:entityId", "Get compliance status"),
            create_request("Get Violations", "GET", "/api/asset/v1/compliance/violations/:entityType/:entityId", "Get compliance violations",
                params=[{"key": "unresolvedOnly", "value": "true"}]),
            create_request("Resolve Violation", "POST", "/api/asset/v1/compliance/violations/:violationId/resolve", "Resolve a violation",
                params=[{"key": "resolvedBy", "value": "admin"}, {"key": "notes", "value": "Resolved"}]),
            create_request("Generate Compliance Report", "GET", "/api/asset/v1/compliance/report/:entityType/:entityId", "Generate compliance report"),
            create_request("Bulk Validation", "POST", "/api/asset/v1/compliance/validate/bulk/:entityType", "Bulk validate entities",
                {"mode": "raw", "raw": json.dumps([1, 2, 3], indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Get Compliance Metrics", "GET", "/api/asset/v1/compliance/metrics", "Get overall compliance metrics"),
            create_request("Get Compliance Metrics by Entity Type", "GET", "/api/asset/v1/compliance/metrics/:entityType", "Get compliance metrics by entity type"),
            create_request("Get Violations Summary", "GET", "/api/asset/v1/compliance/violations/summary", "Get violations summary")
        ]
    }
    collection["item"].append(compliance_folder)
    
    # 16. COMPLIANCE RULES
    compliancerules_folder = {
        "name": "16. Compliance Rules",
        "item": [
            create_request("List All Rules", "GET", "/api/asset/v1/compliance/rules", "Get all compliance rules"),
            create_request("List Rules by Entity Type", "GET", "/api/asset/v1/compliance/rules/entity-type/:entityType", "Get rules by entity type"),
            create_request("Get Rule by ID", "GET", "/api/asset/v1/compliance/rules/:ruleId", "Get rule by ID"),
            create_request("Create Rule", "POST", "/api/asset/v1/compliance/rules", "Create a compliance rule",
                {"mode": "raw", "raw": json.dumps({"ruleCode": "RULE001", "ruleName": "Asset Warranty Required", "entityType": "ASSET", "ruleExpression": "warranty != null"}, indent=2), "options": {"raw": {"language": "json"}}},
                params=[{"key": "createdBy", "value": "admin"}]),
            create_request("Update Rule", "PUT", "/api/asset/v1/compliance/rules/:ruleId", "Update a compliance rule",
                {"mode": "raw", "raw": json.dumps({"ruleCode": "RULE001", "ruleName": "Updated Rule"}, indent=2), "options": {"raw": {"language": "json"}}},
                params=[{"key": "updatedBy", "value": "admin"}]),
            create_request("Delete Rule", "DELETE", "/api/asset/v1/compliance/rules/:ruleId", "Delete a compliance rule",
                params=[{"key": "deletedBy", "value": "admin"}]),
            create_request("Initialize Default Rules", "POST", "/api/asset/v1/compliance/rules/initialize", "Initialize default compliance rules",
                params=[{"key": "createdBy", "value": "SYSTEM"}]),
            create_request("Get Rule Templates", "GET", "/api/asset/v1/compliance/rules/templates", "Get available rule templates")
        ]
    }
    collection["item"].append(compliancerules_folder)
    
    # 17. ASSET SCAN
    assetscan_folder = {
        "name": "17. Asset Scan",
        "item": [
            create_request("Scan Asset (POST)", "POST", "/api/asset/v1/scan", "Scan asset by QR code or barcode",
                {"mode": "raw", "raw": json.dumps({"scanValue": "SN123456", "scanType": "AUTO"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Scan Asset (GET)", "GET", "/api/asset/v1/scan", "Scan asset by QR code or barcode (GET)",
                params=[{"key": "value", "value": "SN123456"}, {"key": "type", "value": "AUTO"}]),
            create_request("Scan and Save Asset", "POST", "/api/asset/v1/scan/save", "Scan QR/barcode and create/update asset with AI agent",
                {"mode": "raw", "raw": json.dumps({"scanValue": "SN123456", "scanType": "AUTO", "userId": "{{userId}}", "username": "{{username}}"}, indent=2), "options": {"raw": {"language": "json"}}})
        ]
    }
    collection["item"].append(assetscan_folder)
    
    # 18. MASTER DATA AGENT
    masterdata_folder = {
        "name": "18. Master Data Agent",
        "item": [
            create_request("Create Category", "POST", "/api/asset/v1/masters/categories", "Create category via master data agent",
                {"mode": "raw", "raw": json.dumps({"categoryName": "Electronics", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Update Category", "PUT", "/api/asset/v1/masters/categories/:categoryId", "Update category",
                params=[{"key": "categoryName", "value": "Electronics Updated"}, {"key": "updatedBy", "value": "admin"}]),
            create_request("Delete Category", "DELETE", "/api/asset/v1/masters/categories/:categoryId", "Delete category",
                params=[{"key": "deletedBy", "value": "admin"}]),
            create_request("Create SubCategory", "POST", "/api/asset/v1/masters/subcategories", "Create subcategory",
                {"mode": "raw", "raw": json.dumps({"subCategoryName": "Laptops", "categoryId": 1, "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete SubCategory", "DELETE", "/api/asset/v1/masters/subcategories/:subCategoryId", "Delete subcategory",
                params=[{"key": "deletedBy", "value": "admin"}]),
            create_request("Create Make", "POST", "/api/asset/v1/masters/makes", "Create make",
                {"mode": "raw", "raw": json.dumps({"makeName": "Dell", "subCategoryId": 1, "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Make", "DELETE", "/api/asset/v1/masters/makes/:makeId", "Delete make",
                params=[{"key": "deletedBy", "value": "admin"}]),
            create_request("Create Model", "POST", "/api/asset/v1/masters/models", "Create model",
                {"mode": "raw", "raw": json.dumps({"modelName": "XPS 15", "makeId": 1, "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delete Model", "DELETE", "/api/asset/v1/masters/models/:modelId", "Delete model",
                params=[{"key": "deletedBy", "value": "admin"}]),
            create_request("Create Vendor", "POST", "/api/asset/v1/masters/vendors", "Create vendor",
                {"mode": "raw", "raw": json.dumps({"vendorName": "Dell Inc", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Create Outlet", "POST", "/api/asset/v1/masters/outlets", "Create outlet",
                {"mode": "raw", "raw": json.dumps({"outletName": "Best Buy", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Create Component", "POST", "/api/asset/v1/masters/components", "Create component",
                params=[{"key": "componentName", "value": "RAM 16GB"}, {"key": "description", "value": "16GB DDR4"}, {"key": "createdBy", "value": "admin"}]),
            create_request("Bulk Create Categories", "POST", "/api/asset/v1/masters/categories/bulk", "Bulk create categories",
                {"mode": "raw", "raw": json.dumps(["Category 1", "Category 2"], indent=2), "options": {"raw": {"language": "json"}}},
                params=[{"key": "createdBy", "value": "admin"}]),
            create_request("Validate Category", "GET", "/api/asset/v1/masters/validate/category/:categoryId", "Validate category exists"),
            create_request("Get Master Data Summary", "GET", "/api/asset/v1/masters/summary", "Get master data summary")
        ]
    }
    collection["item"].append(masterdata_folder)
    
    # 19. AUDIT AGENT
    audit_folder = {
        "name": "19. Audit Agent",
        "item": [
            create_request("Log Audit Event", "POST", "/api/asset/v1/audit/log", "Log an audit event",
                {"mode": "raw", "raw": json.dumps({"username": "admin", "eventMessage": "Asset created", "action": "CREATE", "entityType": "ASSET", "entityId": 1}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Get All Audit Logs", "GET", "/api/asset/v1/audit", "Get all audit logs"),
            create_request("Get Audit Logs by Username", "GET", "/api/asset/v1/audit/username/:username", "Get audit logs by username"),
            create_request("Get Audit Logs by Entity Type", "GET", "/api/asset/v1/audit/entity-type/:entityType", "Get audit logs by entity type"),
            create_request("Get Audit Logs by Date Range", "GET", "/api/asset/v1/audit/date-range", "Get audit logs by date range",
                params=[{"key": "startDate", "value": "2024-01-01T00:00:00"}, {"key": "endDate", "value": "2024-12-31T23:59:59"}]),
            create_request("Get Recent Audit Logs", "GET", "/api/asset/v1/audit/recent", "Get recent audit logs",
                params=[{"key": "limit", "value": "100"}]),
            create_request("Search Audit Logs", "GET", "/api/asset/v1/audit/search", "Search audit logs",
                params=[{"key": "keyword", "value": "asset"}]),
            create_request("Get Audit Statistics", "GET", "/api/asset/v1/audit/statistics", "Get audit statistics"),
            create_request("Cleanup Old Audit Logs", "POST", "/api/asset/v1/audit/cleanup", "Cleanup old audit logs",
                params=[{"key": "daysToKeep", "value": "90"}])
        ]
    }
    collection["item"].append(audit_folder)
    
    # 20. USER ASSET LINK AGENT
    userassetlink_folder = {
        "name": "20. User Asset Link Agent",
        "item": [
            create_request("Link Asset to User", "POST", "/api/asset/v1/user-asset-links/link-asset", "Link asset to user",
                {"mode": "raw", "raw": json.dumps({"assetId": 1, "userId": 1, "username": "user1", "email": "user1@example.com", "mobile": "1234567890", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Link Component to User", "POST", "/api/asset/v1/user-asset-links/link-component", "Link component to user",
                {"mode": "raw", "raw": json.dumps({"componentId": 1, "userId": 1, "username": "user1", "email": "user1@example.com", "mobile": "1234567890", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Delink Asset from User", "POST", "/api/asset/v1/user-asset-links/delink-asset", "Delink asset from user",
                params=[{"key": "assetId", "value": "1"}, {"key": "userId", "value": "1"}, {"key": "updatedBy", "value": "admin"}]),
            create_request("Delink Component from User", "POST", "/api/asset/v1/user-asset-links/delink-component", "Delink component from user",
                params=[{"key": "componentId", "value": "1"}, {"key": "userId", "value": "1"}, {"key": "updatedBy", "value": "admin"}]),
            create_request("Get Assets Assigned to User", "GET", "/api/asset/v1/user-asset-links/user/:userId/assets", "Get assets assigned to user"),
            create_request("Get Components Assigned to User", "GET", "/api/asset/v1/user-asset-links/user/:userId/components", "Get components assigned to user"),
            create_request("Get Asset Assignment History", "GET", "/api/asset/v1/user-asset-links/asset/:assetId/history", "Get asset assignment history"),
            create_request("Get User Assignment History", "GET", "/api/asset/v1/user-asset-links/user/:userId/history", "Get user assignment history"),
            create_request("Check Asset Linked", "GET", "/api/asset/v1/user-asset-links/check/asset/:assetId/user/:userId", "Check if asset is linked to user"),
            create_request("Get Link Statistics", "GET", "/api/asset/v1/user-asset-links/statistics", "Get link statistics"),
            create_request("Bulk Link Assets", "POST", "/api/asset/v1/user-asset-links/bulk-link-assets", "Bulk link assets to user",
                {"mode": "raw", "raw": json.dumps({"assetIds": [1, 2, 3], "userId": 1, "username": "user1", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}})
        ]
    }
    collection["item"].append(userassetlink_folder)
    
    # 21. FILE DOWNLOAD
    filedownload_folder = {
        "name": "21. File Download",
        "item": [
            create_request("Download or View File", "GET", "/api/asset/v1/files/download", "Download or view a file",
                params=[{"key": "filename", "value": "document.pdf"}, {"key": "inline", "value": "false"}])
        ]
    }
    collection["item"].append(filedownload_folder)
    
    return collection

def create_helpdesk_service_collection():
    """Create complete Postman collection for Helpdesk Service"""
    
    collection = {
        "info": {
            "_postman_id": "helpdesk-service-complete-v1",
            "name": "Helpdesk Service - Complete API Collection",
            "description": "Complete Postman collection for Helpdesk Service with all controllers and endpoints",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "_exporter_id": "helpdesk-service"
        },
        "item": [],
        "variable": [
            {"key": "helpdeskbaseUrl", "value": "http://localhost:8084", "type": "string"},
            {"key": "bearerToken", "value": "your-jwt-token-here", "type": "string"}
        ]
    }
    
    def create_request(name, method, path, description="", body=None, params=None):
        headers = [
            {
                "key": "Authorization",
                "value": "Bearer {{bearerToken}}",
                "type": "text"
            },
            {
                "key": "Content-Type",
                "value": "application/json",
                "type": "text"
            }
        ]
        
        url_parts = path.split("/")
        url_path = []
        url_vars = []
        
        for part in url_parts:
            if part.startswith(":"):
                var_name = part[1:]
                url_path.append(part)
                url_vars.append({
                    "key": var_name,
                    "value": "1",
                    "description": f"{var_name.replace('Id', ' ID')}"
                })
            elif part:
                url_path.append(part)
        
        request_obj = {
            "name": name,
            "request": {
                "method": method,
                "header": headers,
                "url": {
                    "raw": "{{helpdeskbaseUrl}}" + "/" + "/".join(url_path),
                    "host": ["{{helpdeskbaseUrl}}"],
                    "path": url_path
                },
                "description": description
            },
            "response": []
        }
        
        if url_vars:
            request_obj["request"]["url"]["variable"] = url_vars
        
        if body:
            request_obj["request"]["body"] = body
        
        if params:
            request_obj["request"]["url"]["query"] = params
        
        return request_obj
    
    # 1. ISSUES
    issues_folder = {
        "name": "1. Issues",
        "item": [
            create_request(
                "Create Issue",
                "POST",
                "/api/helpdesk/issues",
                "Create a new issue ticket",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "title": "Network connectivity issue",
                        "description": "Unable to connect to network",
                        "priority": "HIGH",
                        "relatedService": "ASSET_SERVICE",
                        "reportedBy": "user@example.com"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get All Issues",
                "GET",
                "/api/helpdesk/issues",
                "Retrieve all issues"
            ),
            create_request(
                "Get Issue by ID",
                "GET",
                "/api/helpdesk/issues/:id",
                "Retrieve a specific issue by its ID"
            ),
            create_request(
                "Get Issues by Status",
                "GET",
                "/api/helpdesk/issues/status/:status",
                "Retrieve issues filtered by status",
                params=[{"key": "status", "value": "OPEN", "description": "Issue status"}]
            ),
            create_request(
                "Get Issues by Service",
                "GET",
                "/api/helpdesk/issues/service/:service",
                "Retrieve issues filtered by related service"
            ),
            create_request(
                "Get My Issues",
                "GET",
                "/api/helpdesk/issues/my-issues",
                "Retrieve issues reported by the current user"
            ),
            create_request(
                "Update Issue Status",
                "PATCH",
                "/api/helpdesk/issues/:id/status",
                "Update the status of an issue",
                params=[{"key": "status", "value": "IN_PROGRESS"}]
            ),
            create_request(
                "Assign Issue",
                "PATCH",
                "/api/helpdesk/issues/:id/assign",
                "Assign an issue to a support agent",
                params=[{"key": "assignedTo", "value": "agent@example.com"}]
            ),
            create_request(
                "Resolve Issue",
                "POST",
                "/api/helpdesk/issues/:id/resolve",
                "Resolve an issue with a resolution description",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "resolutionDescription": "Issue resolved by restarting the router",
                        "resolvedBy": "agent@example.com"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Close Issue",
                "PATCH",
                "/api/helpdesk/issues/:id/close",
                "Close an issue"
            )
        ]
    }
    collection["item"].append(issues_folder)
    
    # 2. ESCALATIONS
    escalations_folder = {
        "name": "2. Escalations",
        "item": [
            create_request(
                "Escalate Issue",
                "POST",
                "/api/helpdesk/escalations/issue/:issueId",
                "Manually escalate an issue to a higher support level",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "escalationReason": "Issue not resolved within SLA",
                        "escalatedBy": "agent@example.com",
                        "targetLevel": "LEVEL_2"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Auto-escalate Issue",
                "POST",
                "/api/helpdesk/escalations/issue/:issueId/auto-escalate",
                "Trigger auto-escalation check for an issue"
            ),
            create_request(
                "Get Issue Escalations",
                "GET",
                "/api/helpdesk/escalations/issue/:issueId",
                "Retrieve escalation history for an issue"
            )
        ]
    }
    collection["item"].append(escalations_folder)
    
    # 3. ESCALATION MATRIX
    escalation_matrix_folder = {
        "name": "3. Escalation Matrix",
        "item": [
            create_request(
                "Create Escalation Matrix",
                "POST",
                "/api/helpdesk/escalation-matrix",
                "Create a new escalation matrix entry with SLA configuration",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "relatedService": "ASSET_SERVICE",
                        "priority": "HIGH",
                        "firstResponseTimeMinutes": 30,
                        "resolutionTimeMinutes": 240,
                        "level1EscalationMinutes": 60,
                        "level2EscalationMinutes": 120,
                        "level3EscalationMinutes": 180
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get All Escalation Matrices",
                "GET",
                "/api/helpdesk/escalation-matrix",
                "Retrieve all escalation matrix configurations"
            ),
            create_request(
                "Get Escalation Matrix by ID",
                "GET",
                "/api/helpdesk/escalation-matrix/:id",
                "Retrieve a specific escalation matrix by its ID"
            ),
            create_request(
                "Get Escalation Matrices by Service",
                "GET",
                "/api/helpdesk/escalation-matrix/service/:service",
                "Retrieve escalation matrices for a specific service"
            ),
            create_request(
                "Get Escalation Matrix",
                "GET",
                "/api/helpdesk/escalation-matrix/service/:service/priority/:priority",
                "Get active escalation matrix for service and priority"
            ),
            create_request(
                "Update Escalation Matrix",
                "PUT",
                "/api/helpdesk/escalation-matrix/:id",
                "Update an existing escalation matrix",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "relatedService": "ASSET_SERVICE",
                        "priority": "HIGH",
                        "firstResponseTimeMinutes": 30,
                        "resolutionTimeMinutes": 240
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Delete Escalation Matrix",
                "DELETE",
                "/api/helpdesk/escalation-matrix/:id",
                "Delete an escalation matrix"
            )
        ]
    }
    collection["item"].append(escalation_matrix_folder)
    
    # 4. SLA
    sla_folder = {
        "name": "4. SLA",
        "item": [
            create_request(
                "Get SLA Tracking",
                "GET",
                "/api/helpdesk/sla/issue/:issueId",
                "Retrieve SLA tracking information for an issue"
            ),
            create_request(
                "Get SLA Breaches",
                "GET",
                "/api/helpdesk/sla/breaches",
                "Retrieve all issues with SLA breaches"
            ),
            create_request(
                "Record First Response",
                "POST",
                "/api/helpdesk/sla/issue/:issueId/first-response",
                "Record the first response time for an issue"
            )
        ]
    }
    collection["item"].append(sla_folder)
    
    # 5. FAQs
    faqs_folder = {
        "name": "5. FAQs",
        "item": [
            create_request(
                "Create FAQ",
                "POST",
                "/api/helpdesk/faqs",
                "Add a new frequently asked question",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "question": "How do I reset my password?",
                        "answer": "Click on forgot password link",
                        "category": "Authentication",
                        "relatedService": "ASSET_SERVICE"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get All FAQs",
                "GET",
                "/api/helpdesk/faqs",
                "Retrieve all FAQs"
            ),
            create_request(
                "Get FAQ by ID",
                "GET",
                "/api/helpdesk/faqs/:id",
                "Retrieve a specific FAQ by its ID"
            ),
            create_request(
                "Get FAQs by Service",
                "GET",
                "/api/helpdesk/faqs/service/:service",
                "Retrieve FAQs filtered by related service"
            ),
            create_request(
                "Get FAQs by Category",
                "GET",
                "/api/helpdesk/faqs/category/:category",
                "Retrieve FAQs filtered by category"
            ),
            create_request(
                "Search FAQs",
                "GET",
                "/api/helpdesk/faqs/search",
                "Search FAQs by keyword",
                params=[{"key": "keyword", "value": "password"}]
            ),
            create_request(
                "Search FAQs by Service",
                "GET",
                "/api/helpdesk/faqs/service/:service/search",
                "Search FAQs by service and keyword",
                params=[{"key": "keyword", "value": "password"}]
            ),
            create_request(
                "Update FAQ",
                "PUT",
                "/api/helpdesk/faqs/:id",
                "Update an existing FAQ",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "question": "Updated question",
                        "answer": "Updated answer"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Mark FAQ as Helpful",
                "POST",
                "/api/helpdesk/faqs/:id/helpful",
                "Increment the helpful count for an FAQ"
            ),
            create_request(
                "Delete FAQ",
                "DELETE",
                "/api/helpdesk/faqs/:id",
                "Delete an FAQ"
            )
        ]
    }
    collection["item"].append(faqs_folder)
    
    # 6. QUERIES
    queries_folder = {
        "name": "6. Queries",
        "item": [
            create_request(
                "Create Query",
                "POST",
                "/api/helpdesk/queries",
                "Submit a new query",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "question": "How do I create an asset?",
                        "relatedService": "ASSET_SERVICE",
                        "askedBy": "user@example.com"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get All Queries",
                "GET",
                "/api/helpdesk/queries",
                "Retrieve all queries"
            ),
            create_request(
                "Get Query by ID",
                "GET",
                "/api/helpdesk/queries/:id",
                "Retrieve a specific query by its ID"
            ),
            create_request(
                "Get Queries by Status",
                "GET",
                "/api/helpdesk/queries/status/:status",
                "Retrieve queries filtered by status"
            ),
            create_request(
                "Get Queries by Service",
                "GET",
                "/api/helpdesk/queries/service/:service",
                "Retrieve queries filtered by related service"
            ),
            create_request(
                "Get My Queries",
                "GET",
                "/api/helpdesk/queries/my-queries",
                "Retrieve queries asked by the current user"
            ),
            create_request(
                "Answer Query",
                "POST",
                "/api/helpdesk/queries/:id/answer",
                "Provide an answer to a pending query",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "answer": "You can create an asset using the asset creation API",
                        "answeredBy": "agent@example.com"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Close Query",
                "PATCH",
                "/api/helpdesk/queries/:id/close",
                "Close a query"
            )
        ]
    }
    collection["item"].append(queries_folder)
    
    # 7. CHATBOT
    chatbot_folder = {
        "name": "7. Chatbot",
        "item": [
            create_request(
                "Send Message to Chatbot",
                "POST",
                "/api/helpdesk/chatbot/message",
                "Send a message to the chatbot and get a response",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "message": "How do I create an asset?",
                        "sessionId": "session-123"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get Session History",
                "GET",
                "/api/helpdesk/chatbot/session/:sessionId",
                "Retrieve conversation history for a chatbot session"
            )
        ]
    }
    collection["item"].append(chatbot_folder)
    
    # 8. SERVICE KNOWLEDGE
    knowledge_folder = {
        "name": "8. Service Knowledge",
        "item": [
            create_request(
                "Create Knowledge Entry",
                "POST",
                "/api/helpdesk/knowledge",
                "Add new knowledge about a service",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "title": "Asset Creation Process",
                        "content": "To create an asset, use the POST /api/asset/v1/assets endpoint",
                        "relatedService": "ASSET_SERVICE",
                        "category": "Asset Management"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Get All Knowledge",
                "GET",
                "/api/helpdesk/knowledge",
                "Retrieve all knowledge entries"
            ),
            create_request(
                "Get Knowledge by ID",
                "GET",
                "/api/helpdesk/knowledge/:id",
                "Retrieve a specific knowledge entry by its ID"
            ),
            create_request(
                "Get Knowledge by Service",
                "GET",
                "/api/helpdesk/knowledge/service/:service",
                "Retrieve knowledge entries filtered by service"
            ),
            create_request(
                "Search Knowledge",
                "GET",
                "/api/helpdesk/knowledge/service/:service/search",
                "Search knowledge entries by service and keyword",
                params=[{"key": "keyword", "value": "asset"}]
            ),
            create_request(
                "Update Knowledge",
                "PUT",
                "/api/helpdesk/knowledge/:id",
                "Update an existing knowledge entry",
                {
                    "mode": "raw",
                    "raw": json.dumps({
                        "title": "Updated Title",
                        "content": "Updated content"
                    }, indent=2),
                    "options": {"raw": {"language": "json"}}
                }
            ),
            create_request(
                "Delete Knowledge",
                "DELETE",
                "/api/helpdesk/knowledge/:id",
                "Delete a knowledge entry"
            )
        ]
    }
    collection["item"].append(knowledge_folder)
    
    return collection

if __name__ == "__main__":
    # Generate Asset Service Collection
    asset_collection = create_asset_service_collection()
    with open("asset-service/docs/postman/Asset_Service_Complete_API_Collection.postman_collection.json", "w") as f:
        json.dump(asset_collection, f, indent=2)
    print("✅ Asset Service Postman collection created!")
    
    # Generate Helpdesk Service Collection
    helpdesk_collection = create_helpdesk_service_collection()
    with open("helpdesk-service/docs/postman/Helpdesk_Service_Complete_API_Collection.postman_collection.json", "w") as f:
        json.dump(helpdesk_collection, f, indent=2)
    print("✅ Helpdesk Service Postman collection created!")
    
    print("\n🎉 All Postman collections generated successfully!")

