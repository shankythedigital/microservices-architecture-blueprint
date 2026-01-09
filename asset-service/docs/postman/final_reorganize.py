#!/usr/bin/env python3
"""
Final reorganization script to properly organize collection by controller structure.
Maps folders to controllers, removes duplicates, and ensures proper numbering.
"""

import json
import sys
from collections import OrderedDict

# Controller mapping - maps controller to folder names in collection
CONTROLLER_MAPPING = [
    # 1. AssetController
    {
        "controller": "AssetController",
        "basePath": "/api/asset/v1/assets",
        "folders": ["1. Assets", "2. Complete Asset Creation"]
    },
    
    # 2. CategoryController
    {
        "controller": "CategoryController",
        "basePath": "/api/asset/v1/categories",
        "folders": ["3. Categories"]
    },
    
    # 3. SubCategoryController
    {
        "controller": "SubCategoryController",
        "basePath": "/api/asset/v1/subcategories",
        "folders": ["4. SubCategories"]
    },
    
    # 4. MakeController
    {
        "controller": "MakeController",
        "basePath": "/api/asset/v1/makes",
        "folders": ["5. Makes"]
    },
    
    # 5. ModelController
    {
        "controller": "ModelController",
        "basePath": "/api/asset/v1/models",
        "folders": ["6. Models"]
    },
    
    # 6. VendorController
    {
        "controller": "VendorController",
        "basePath": "/api/asset/v1/vendors",
        "folders": ["7. Vendors"]
    },
    
    # 7. OutletController
    {
        "controller": "OutletController",
        "basePath": "/api/asset/v1/outlets",
        "folders": ["8. Outlets"]
    },
    
    # 8. ComponentController
    {
        "controller": "ComponentController",
        "basePath": "/api/asset/v1/components",
        "folders": ["9. Components"]
    },
    
    # 9. UserLinkController
    {
        "controller": "UserLinkController",
        "basePath": "/api/asset/v1/userlinks",
        "folders": [
            "1. Link Entity (Universal)",
            "2. Delink Entity (Universal)",
            "3. Link Multiple Entities",
            "4. Delink Multiple Entities",
            "5. Query Operations",
            "6. Master Data API",
            "1. Get All Master Data",
            "2. Get Master Data by User ID",
            "3. Need Your Attention"
        ]
    },
    
    # 10. AssetWarrantyController
    {
        "controller": "AssetWarrantyController",
        "basePath": "/api/asset/v1/warranty",
        "folders": ["11. Warranty", "1. Warranty Operations"]
    },
    
    # 11. AssetAmcController
    {
        "controller": "AssetAmcController",
        "basePath": "/api/asset/v1/amc",
        "folders": ["12. AMC", "2. AMC Operations"]
    },
    
    # 12. DocumentController
    {
        "controller": "DocumentController",
        "basePath": "/api/asset/v1/documents",
        "folders": ["13. Documents", "12. Documents"]
    },
    
    # 13. FileDownloadController
    {
        "controller": "FileDownloadController",
        "basePath": "/api/asset/v1/files",
        "folders": []  # Will be added if exists
    },
    
    # 14. ComplianceController
    {
        "controller": "ComplianceController",
        "basePath": "/api/asset/v1/compliance",
        "folders": [
            "Compliance Validation",
            "Compliance Status",
            "Compliance Violations",
            "Compliance Reports",
            "Compliance Metrics"
        ]
    },
    
    # 15. ComplianceRuleController
    {
        "controller": "ComplianceRuleController",
        "basePath": "/api/asset/v1/compliance/rules",
        "folders": ["16. Compliance Rules", "Compliance Rules", "14. Compliance Rules"]
    },
    
    # 16. EntityTypeController
    {
        "controller": "EntityTypeController",
        "basePath": "/api/asset/v1/entity-types",
        "folders": ["17. Entity Types", "15. Entity Types"]
    },
    
    # 17. StatusController
    {
        "controller": "StatusController",
        "basePath": "/api/asset/v1/statuses",
        "folders": ["18. Status", "16. Status"]
    },
    
    # 18. MasterDataAgentController
    {
        "controller": "MasterDataAgentController",
        "basePath": "/api/asset/v1/masters",
        "folders": ["19. Master Data Agent", "17. Master Data Agent"]
    },
    
    # 19. UserAssetLinkAgentController
    {
        "controller": "UserAssetLinkAgentController",
        "basePath": "/api/asset/v1/user-asset-links",
        "folders": ["20. User Asset Link Agent", "18. User Asset Link Agent"]
    },
    
    # 20. AuditAgentController
    {
        "controller": "AuditAgentController",
        "basePath": "/api/asset/v1/audit",
        "folders": ["21. Audit Agent", "19. Audit Agent"]
    }
]

def reorganize_collection():
    """Reorganize collection by controller structure"""
    
    # Read consolidated collection
    with open('Asset_Service_Consolidated.postman_collection.json', 'r') as f:
        collection = json.load(f)
    
    # Create a map of folder names to folder objects
    folder_map = {}
    for item in collection.get('item', []):
        folder_name = item.get('name', '')
        folder_map[folder_name] = item
    
    # Reorganize based on controller mapping
    reorganized_items = []
    used_folders = set()
    folder_counter = 1
    
    for controller_info in CONTROLLER_MAPPING:
        controller_name = controller_info["controller"]
        folder_names = controller_info["folders"]
        
        # Find and add folders for this controller
        controller_folders = []
        for folder_name in folder_names:
            if folder_name in folder_map and folder_name not in used_folders:
                folder = folder_map[folder_name]
                # Rename with proper numbering
                folder_copy = json.loads(json.dumps(folder))
                folder_copy["name"] = f"{folder_counter}. {controller_name.replace('Controller', '')}"
                controller_folders.append(folder_copy)
                used_folders.add(folder_name)
        
        # If no folders found, try to find by base path
        if not controller_folders:
            for folder_name, folder in folder_map.items():
                if folder_name not in used_folders:
                    # Check if any request in folder matches the base path
                    for request in folder.get('item', []):
                        url = request.get('request', {}).get('url', {}).get('raw', '')
                        if controller_info["basePath"] in url:
                            folder_copy = json.loads(json.dumps(folder))
                            folder_copy["name"] = f"{folder_counter}. {controller_name.replace('Controller', '')}"
                            controller_folders.append(folder_copy)
                            used_folders.add(folder_name)
                            break
        
        if controller_folders:
            # If multiple folders for same controller, merge them
            if len(controller_folders) == 1:
                reorganized_items.append(controller_folders[0])
            else:
                # Merge multiple folders into one
                merged_folder = {
                    "name": f"{folder_counter}. {controller_name.replace('Controller', '')}",
                    "item": []
                }
                for folder in controller_folders:
                    merged_folder["item"].extend(folder.get('item', []))
                reorganized_items.append(merged_folder)
            folder_counter += 1
    
    # Add any remaining folders
    for folder_name, folder in folder_map.items():
        if folder_name not in used_folders:
            folder_copy = json.loads(json.dumps(folder))
            folder_copy["name"] = f"{folder_counter}. {folder_name}"
            reorganized_items.append(folder_copy)
            folder_counter += 1
    
    # Update collection
    collection['item'] = reorganized_items
    
    # Update description
    collection['info']['description'] = """Complete consolidated Postman collection for Asset Management Service, organized by controller structure.

**Controller-Based Organization (20 Controllers):**
1. AssetController - Asset CRUD, Search, Bulk, Complete Creation
2. CategoryController - Category management
3. SubCategoryController - SubCategory management
4. MakeController - Make management
5. ModelController - Model management
6. VendorController - Vendor management
7. OutletController - Outlet management
8. ComponentController - Component management
9. UserLinkController - User linking, Master Data, Need Your Attention
10. AssetWarrantyController - Warranty operations
11. AssetAmcController - AMC operations
12. DocumentController - Document upload/management
13. FileDownloadController - File download operations
14. ComplianceController - Compliance validation, status, violations, reports, metrics
15. ComplianceRuleController - Compliance rules management
16. EntityTypeController - Entity type management
17. StatusController - Status management
18. MasterDataAgentController - Master data agent operations
19. UserAssetLinkAgentController - User asset link agent operations
20. AuditAgentController - Audit logging and tracking

**Features:**
- All endpoints from all 20 controllers
- Organized by controller structure
- Environment and global variables properly configured
- Comprehensive request examples
- Consistent variable naming (assetbaseUrl)

**Environment Variables:**
See Asset_Service_Consolidated_Environment.postman_environment.json for all 39 environment variables."""
    
    return collection

def ensure_global_variables(collection):
    """Ensure all necessary global/collection variables are present and sorted"""
    
    required_variables = OrderedDict([
        ("assetbaseUrl", {
            "key": "assetbaseUrl",
            "value": "http://localhost:8083",
            "type": "string",
            "description": "Base URL for Asset Service API. Default: http://localhost:8083"
        }),
        ("accessToken", {
            "key": "accessToken",
            "value": "",
            "type": "string",
            "description": "JWT Bearer token obtained from auth-service login endpoint"
        }),
        ("userId", {
            "key": "userId",
            "value": "1",
            "type": "string",
            "description": "Current user ID for operations"
        }),
        ("username", {
            "key": "username",
            "value": "admin",
            "type": "string",
            "description": "Current username for operations"
        }),
        ("projectType", {
            "key": "projectType",
            "value": "ASSET_SERVICE",
            "type": "string",
            "description": "Project type for notifications and audit"
        })
    ])
    
    # Get existing variables
    existing_vars = {v.get('key'): v for v in collection.get('variable', [])}
    
    # Start with required variables
    final_variables = []
    for key, var_def in required_variables.items():
        if key in existing_vars:
            final_variables.append(existing_vars[key])
        else:
            final_variables.append(var_def)
    
    # Add remaining variables sorted alphabetically
    remaining_keys = sorted([k for k in existing_vars.keys() if k not in required_variables])
    for key in remaining_keys:
        final_variables.append(existing_vars[key])
    
    collection['variable'] = final_variables
    
    return collection

if __name__ == "__main__":
    try:
        print("🔄 Final reorganization by controller structure...")
        
        # Reorganize
        collection = reorganize_collection()
        
        # Ensure global variables
        collection = ensure_global_variables(collection)
        
        # Write reorganized collection
        with open('Asset_Service_Consolidated.postman_collection.json', 'w') as f:
            json.dump(collection, f, indent=2)
        
        print("✅ Collection reorganized by controllers!")
        print(f"   Total folders: {len(collection['item'])}")
        print(f"   Total collection variables: {len(collection.get('variable', []))}")
        
        # Show folder order
        print("\n📋 Folder Organization (by Controller):")
        for i, item in enumerate(collection['item'], 1):
            folder_name = item.get('name', 'Unknown')
            request_count = len(item.get('item', []))
            print(f"   {i:2d}. {folder_name} ({request_count} requests)")
        
        # Verify environment
        with open('Asset_Service_Consolidated_Environment.postman_environment.json', 'r') as f:
            env = json.load(f)
        
        print(f"\n✅ Environment Variables: {len(env.get('values', []))}")
        print("   Key variables: assetbaseUrl, accessToken, userId, username, projectType")
        print("   Plus all entity IDs, statuses, dates, and other variables")
        
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

