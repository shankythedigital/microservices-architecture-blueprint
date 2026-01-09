#!/usr/bin/env python3
"""
Script to consolidate all Asset Service Postman collections and environments
into a single comprehensive collection and environment file.
"""

import json
import sys
from collections import OrderedDict

def merge_collections():
    """Merge all Postman collections into one"""
    
    # Read all collections
    collections = {
        'main': json.load(open('Asset_Service_API.postman_collection.json', 'r')),
        'complete': json.load(open('Complete_Asset_Creation_API.postman_collection.json', 'r')),
        'compliance': json.load(open('Compliance_Agent_API.postman_collection.json', 'r')),
        'master': json.load(open('Master_Data_API.postman_collection.json', 'r')),
        'userlink': json.load(open('UserLinkController.postman_collection.json', 'r')),
        'warranty': json.load(open('Warranty_AMC_Controllers.postman_collection.json', 'r'))
    }
    
    # Create consolidated collection
    consolidated = {
        "info": {
            "_postman_id": "asset-service-consolidated-complete",
            "name": "Asset Service - Complete Consolidated API Collection",
            "description": """Complete consolidated Postman collection for Asset Management Service.

**This collection includes:**
- All Asset CRUD operations (Create, Read, Update, Delete, Search, Bulk operations)
- Complete Asset Creation (one-go endpoint with warranty, document, and user assignment)
- Category, SubCategory, Make, Model management
- Vendor and Outlet management
- Component management
- Warranty and AMC operations
- Document upload and management
- User Link operations (link/delink assets, components, etc.)
- Master Data API (comprehensive data retrieval)
- Need Your Attention API (attention indicators)
- Compliance validation and rules
- Audit logging
- Status and Entity Type management
- File download operations

**Features:**
- All endpoints from all controllers
- Environment variables for easy configuration
- Comprehensive request examples
- Organized by functional areas

**Environment Variables:**
See Asset_Service_Consolidated_Environment.postman_environment.json for all available variables.""",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "_exporter_id": "32725094"
        },
        "item": [],
        "variable": []
    }
    
    # Merge items from all collections
    folder_order = [
        ("1. Assets", "main", "1. Assets"),
        ("2. Complete Asset Creation", "complete", "1. Complete Asset Creation"),
        ("3. Categories", "main", "2. Categories"),
        ("4. SubCategories", "main", "3. SubCategories"),
        ("5. Makes", "main", "4. Makes"),
        ("6. Models", "main", "5. Models"),
        ("7. Vendors", "main", "6. Vendors"),
        ("8. Outlets", "main", "7. Outlets"),
        ("9. Components", "main", "8. Components"),
        ("10. User Links", "userlink", None),  # All items
        ("11. Warranty", "warranty", "1. Warranty Operations"),
        ("12. AMC", "warranty", "2. AMC Operations"),
        ("13. Documents", "main", "12. Documents"),
        ("14. Master Data", "master", None),  # All items
        ("15. Compliance", "compliance", None),  # All items
        ("16. Compliance Rules", "main", "14. Compliance Rules"),
        ("17. Entity Types", "main", "15. Entity Types"),
        ("18. Status", "main", "16. Status"),
        ("19. Master Data Agent", "main", "17. Master Data Agent"),
        ("20. User Asset Link Agent", "main", "18. User Asset Link Agent"),
        ("21. Audit Agent", "main", "19. Audit Agent")
    ]
    
    for folder_name, coll_key, source_folder_name in folder_order:
        if coll_key not in collections:
            continue
            
        coll = collections[coll_key]
        
        if source_folder_name is None:
            # Add all items from collection
            for item in coll.get("item", []):
                consolidated["item"].append(item)
        else:
            # Find specific folder
            for item in coll.get("item", []):
                if item.get("name") == source_folder_name:
                    # Rename folder
                    item_copy = json.loads(json.dumps(item))
                    item_copy["name"] = folder_name
                    consolidated["item"].append(item_copy)
                    break
    
    # Merge variables from all collections (deduplicate)
    all_variables = {}
    for coll in collections.values():
        for var in coll.get("variable", []):
            key = var.get("key")
            if key not in all_variables:
                all_variables[key] = var
    
    consolidated["variable"] = list(all_variables.values())
    
    return consolidated

def merge_environments():
    """Merge all environment files into one"""
    
    # Read all environments
    envs = {
        'main': json.load(open('Asset_Service_Environment.postman_environment.json', 'r')),
        'compliance': json.load(open('Compliance_Agent_Environment.postman_environment.json', 'r')),
        'warranty': json.load(open('Warranty_AMC_Environment.postman_environment.json', 'r'))
    }
    
    # Create consolidated environment
    consolidated_env = {
        "id": "asset-service-consolidated-env",
        "name": "Asset Service - Consolidated Environment",
        "values": [],
        "_postman_variable_scope": "environment"
    }
    
    # Merge all environment variables (deduplicate by key)
    all_env_vars = {}
    for env in envs.values():
        for var in env.get("values", []):
            key = var.get("key")
            if key not in all_env_vars:
                all_env_vars[key] = var
            else:
                # Merge descriptions if available
                if var.get("description") and not all_env_vars[key].get("description"):
                    all_env_vars[key]["description"] = var["description"]
    
    consolidated_env["values"] = list(all_env_vars.values())
    
    return consolidated_env

if __name__ == "__main__":
    try:
        print("🔄 Consolidating Postman collections and environments...")
        
        # Merge collections
        consolidated_collection = merge_collections()
        
        # Write consolidated collection
        with open('Asset_Service_Consolidated.postman_collection.json', 'w') as f:
            json.dump(consolidated_collection, f, indent=2)
        
        print(f"✅ Created consolidated collection: Asset_Service_Consolidated.postman_collection.json")
        print(f"   Total folders: {len(consolidated_collection['item'])}")
        print(f"   Total variables: {len(consolidated_collection['variable'])}")
        
        # Merge environments
        consolidated_env = merge_environments()
        
        # Write consolidated environment
        with open('Asset_Service_Consolidated_Environment.postman_environment.json', 'w') as f:
            json.dump(consolidated_env, f, indent=2)
        
        print(f"✅ Created consolidated environment: Asset_Service_Consolidated_Environment.postman_environment.json")
        print(f"   Total environment variables: {len(consolidated_env['values'])}")
        
        print("\n🎉 Consolidation complete!")
        
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

