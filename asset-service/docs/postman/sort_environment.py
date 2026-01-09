#!/usr/bin/env python3
"""Sort environment variables logically"""

import json
import sys

def sort_environment():
    """Sort environment variables in logical order"""
    
    with open('Asset_Service_Consolidated_Environment.postman_environment.json', 'r') as f:
        env = json.load(f)
    
    # Define sort order
    sort_order = [
        # Base configuration
        'assetbaseUrl',
        'accessToken',
        
        # User context
        'userId',
        'username',
        'projectType',
        
        # Entity IDs (alphabetically)
        'amcId',
        'assetId',
        'categoryId',
        'componentId',
        'documentId',
        'entityId',
        'entityTypeId',
        'makeId',
        'modelId',
        'outletId',
        'ruleId',
        'statusId',
        'subCategoryId',
        'targetUserId',
        'targetUsername',
        'vendorId',
        'violationId',
        'warrantyId',
        
        # Entity types and codes
        'entityType',
        'entityTypeCode',
        'statusCategory',
        'statusCode',
        
        # Status values
        'amcStatus',
        'warrantyStatus',
        
        # Warranty/AMC details
        'warrantyProvider',
        'warrantyTerms',
        
        # Dates
        'startDate',
        'endDate',
        
        # Document
        'docType',
        'filename',
        
        # Search and other
        'searchKeyword',
        'authToken',
        'assetId2'
    ]
    
    # Create order map
    order_map = {var: i for i, var in enumerate(sort_order)}
    
    # Sort variables
    def get_sort_key(var):
        key = var.get('key', '')
        return (order_map.get(key, 999), key)
    
    env['values'] = sorted(env.get('values', []), key=get_sort_key)
    
    with open('Asset_Service_Consolidated_Environment.postman_environment.json', 'w') as f:
        json.dump(env, f, indent=2)
    
    print(f"✅ Environment variables sorted!")
    print(f"   Total variables: {len(env['values'])}")

if __name__ == "__main__":
    try:
        print("🔄 Sorting environment variables...")
        sort_environment()
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

