#!/usr/bin/env python3
"""
Script to reorganize the consolidated Postman collection based on asset-service controller structure.
Sorts folders by controller order and ensures proper organization.
"""

import json
import sys

# Controller order based on actual controller files
CONTROLLER_ORDER = [
    # Core Asset Management
    ("AssetController", "/api/asset/v1/assets", [
        "1. Assets",
        "2. Complete Asset Creation"
    ]),
    
    # Master Data Hierarchy
    ("CategoryController", "/api/asset/v1/categories", ["3. Categories"]),
    ("SubCategoryController", "/api/asset/v1/subcategories", ["4. SubCategories"]),
    ("MakeController", "/api/asset/v1/makes", ["5. Makes"]),
    ("ModelController", "/api/asset/v1/models", ["6. Models"]),
    
    # Vendor & Outlet Management
    ("VendorController", "/api/asset/v1/vendors", ["7. Vendors"]),
    ("OutletController", "/api/asset/v1/outlets", ["8. Outlets"]),
    
    # Component Management
    ("ComponentController", "/api/asset/v1/components", ["9. Components"]),
    
    # User Linking
    ("UserLinkController", "/api/asset/v1/userlinks", [
        "10. User Links",
        "11. Master Data API",
        "12. Need Your Attention"
    ]),
    
    # Warranty & AMC
    ("AssetWarrantyController", "/api/asset/v1/warranty", ["13. Warranty"]),
    ("AssetAmcController", "/api/asset/v1/amc", ["14. AMC"]),
    
    # Documents & Files
    ("DocumentController", "/api/asset/v1/documents", ["15. Documents"]),
    ("FileDownloadController", "/api/asset/v1/files", ["16. File Download"]),
    
    # Compliance
    ("ComplianceController", "/api/asset/v1/compliance", [
        "17. Compliance Validation",
        "18. Compliance Status",
        "19. Compliance Violations",
        "20. Compliance Reports",
        "21. Compliance Metrics"
    ]),
    ("ComplianceRuleController", "/api/asset/v1/compliance/rules", ["22. Compliance Rules"]),
    
    # System Configuration
    ("EntityTypeController", "/api/asset/v1/entity-types", ["23. Entity Types"]),
    ("StatusController", "/api/asset/v1/statuses", ["24. Status"]),
    
    # Agent Controllers
    ("MasterDataAgentController", "/api/asset/v1/masters", ["25. Master Data Agent"]),
    ("UserAssetLinkAgentController", "/api/asset/v1/user-asset-links", ["26. User Asset Link Agent"]),
    ("AuditAgentController", "/api/asset/v1/audit", ["27. Audit Agent"])
]

def reorganize_collection():
    """Reorganize collection based on controller structure"""
    
    # Read consolidated collection
    with open('Asset_Service_Consolidated.postman_collection.json', 'r') as f:
        collection = json.load(f)
    
    # Create a map of folder names to folder objects
    folder_map = {}
    for item in collection.get('item', []):
        folder_name = item.get('name', '')
        folder_map[folder_name] = item
    
    # Reorganize based on controller order
    reorganized_items = []
    used_folders = set()
    
    for controller_name, base_path, folder_names in CONTROLLER_ORDER:
        for folder_name in folder_names:
            if folder_name in folder_map and folder_name not in used_folders:
                reorganized_items.append(folder_map[folder_name])
                used_folders.add(folder_name)
    
    # Add any remaining folders that weren't in the controller order
    for item in collection.get('item', []):
        folder_name = item.get('name', '')
        if folder_name not in used_folders:
            reorganized_items.append(item)
    
    # Update collection
    collection['item'] = reorganized_items
    
    # Update description with controller-based organization
    collection['info']['description'] = """Complete consolidated Postman collection for Asset Management Service, organized by controller structure.

**Controller-Based Organization:**
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
- All endpoints from all controllers
- Organized by controller structure
- Environment variables for easy configuration
- Comprehensive request examples
- Consistent variable naming

**Environment Variables:**
See Asset_Service_Consolidated_Environment.postman_environment.json for all available variables."""
    
    return collection

def ensure_global_variables(collection):
    """Ensure all necessary global/collection variables are present"""
    
    required_variables = {
        "assetbaseUrl": {
            "key": "assetbaseUrl",
            "value": "http://localhost:8083",
            "type": "string",
            "description": "Base URL for Asset Service API. Default: http://localhost:8083"
        },
        "accessToken": {
            "key": "accessToken",
            "value": "",
            "type": "string",
            "description": "JWT Bearer token obtained from auth-service login endpoint"
        },
        "userId": {
            "key": "userId",
            "value": "1",
            "type": "string",
            "description": "Current user ID for operations"
        },
        "username": {
            "key": "username",
            "value": "admin",
            "type": "string",
            "description": "Current username for operations"
        },
        "projectType": {
            "key": "projectType",
            "value": "ASSET_SERVICE",
            "type": "string",
            "description": "Project type for notifications and audit"
        }
    }
    
    # Get existing variables
    existing_vars = {v.get('key'): v for v in collection.get('variable', [])}
    
    # Merge required variables
    for key, var_def in required_variables.items():
        if key not in existing_vars:
            collection.setdefault('variable', []).append(var_def)
        else:
            # Update description if needed
            if 'description' not in existing_vars[key] or not existing_vars[key]['description']:
                existing_vars[key]['description'] = var_def['description']
    
    # Sort variables by key
    collection['variable'] = sorted(collection.get('variable', []), key=lambda x: x.get('key', ''))
    
    return collection

if __name__ == "__main__":
    try:
        print("🔄 Reorganizing collection by controller structure...")
        
        # Reorganize
        collection = reorganize_collection()
        
        # Ensure global variables
        collection = ensure_global_variables(collection)
        
        # Write reorganized collection
        with open('Asset_Service_Consolidated.postman_collection.json', 'w') as f:
            json.dump(collection, f, indent=2)
        
        print("✅ Collection reorganized!")
        print(f"   Total folders: {len(collection['item'])}")
        print(f"   Total collection variables: {len(collection.get('variable', []))}")
        
        # Show folder order
        print("\n📋 Folder Organization (by Controller):")
        for i, item in enumerate(collection['item'], 1):
            folder_name = item.get('name', 'Unknown')
            request_count = len(item.get('item', []))
            print(f"   {i:2d}. {folder_name} ({request_count} requests)")
        
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

