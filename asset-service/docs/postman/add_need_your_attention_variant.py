#!/usr/bin/env python3
"""Add Need Your Attention endpoint variant for logged-in user"""

import json
import sys

def add_need_your_attention_variant(collection):
    """Add Need Your Attention endpoint with note about login user"""
    
    # Find UserLink folder
    userlink_folder = None
    for item in collection.get('item', []):
        if 'UserLink' in item.get('name', ''):
            userlink_folder = item
            break
    
    if not userlink_folder:
        print("  UserLink folder not found!")
        return collection
    
    # Check if Need Your Attention already exists
    existing_names = {req.get('name') for req in userlink_folder.get('item', [])}
    
    # Find the existing Need Your Attention request
    need_attention_req = None
    for req in userlink_folder.get('item', []):
        if 'Need Your Attention' in req.get('name', ''):
            need_attention_req = req
            break
    
    if need_attention_req:
        # Update description to clarify it's for logged-in user
        description = need_attention_req.get('request', {}).get('description', '')
        if 'login user' not in description.lower() and 'logged-in' not in description.lower():
            new_description = description + "\n\n**Note:** This endpoint automatically extracts the logged-in user information from the Authorization token for audit purposes. The data returned includes all entities, but the audit section will contain the login userId and loginUsername extracted from the JWT token."
            need_attention_req['request']['description'] = new_description
            print("  Updated existing Need Your Attention request description")
    else:
        # Add new request
        new_request = {
            "name": "Get Need Your Attention (For Logged-In User)",
            "request": {
                "method": "GET",
                "header": [
                    {
                        "key": "Authorization",
                        "value": "Bearer {{accessToken}}",
                        "type": "text",
                        "description": "JWT Bearer token from auth-service. The token is used to extract login userId and username for audit purposes."
                    },
                    {
                        "key": "Content-Type",
                        "value": "application/json",
                        "type": "text"
                    }
                ],
                "url": {
                    "raw": "{{assetbaseUrl}}/api/asset/v1/userlinks/need-your-attention",
                    "host": ["{{assetbaseUrl}}"],
                    "path": ["api", "asset", "v1", "userlinks", "need-your-attention"]
                },
                "description": """Get comprehensive 'Need Your Attention' data for the logged-in user.

**Endpoint:** GET /api/asset/v1/userlinks/need-your-attention

**Authentication:**
- Requires Authorization header with Bearer token
- The endpoint automatically extracts the logged-in user information from the token
- Login userId and username are included in the audit section of the response

**This endpoint returns:**
- **All Master Data:**
  - Users (from asset user links)
  - Assets (with category, subcategory, make, model, status, serial number, purchase date)
  - Components
  - Warranties (with asset details, dates, provider, status, terms)
  - AMCs (with asset details, dates, status)
  - Makes (with subcategory info)
  - Models (with make info)
  - Categories
  - Sub-categories (with category info)
  - Vendors (with contact details, address)
  - Outlets (with address, contact info, vendor info)
  - Statuses (with code, description, category)

- **Summary Counts:**
  - Total counts for all entity types

- **Attention Indicators:**
  - `expiringWarranties`: Warranties expiring within 30 days
  - `expiringWarrantiesCount`: Count of expiring warranties
  - `expiringAmcs`: AMCs expiring within 30 days
  - `expiringAmcsCount`: Count of expiring AMCs
  - `assetsWithoutWarranty`: Assets that don't have a warranty
  - `assetsWithoutWarrantyCount`: Count of assets without warranty
  - `assetsWithoutAmc`: Assets that don't have an AMC
  - `assetsWithoutAmcCount`: Count of assets without AMC
  - `unassignedAssets`: Assets not assigned to any user
  - `unassignedAssetsCount`: Count of unassigned assets

- **Audit Information:**
  - `loginUserId`: User ID extracted from JWT token
  - `loginUsername`: Username extracted from JWT token
  - `requestedAt`: Timestamp of the request
  - `requestType`: "NEED_YOUR_ATTENTION"

**Response Structure:**
```json
{
  "success": true,
  "message": "Need Your Attention data retrieved successfully",
  "data": {
    "users": [...],
    "assets": [...],
    "components": [...],
    "warranties": [...],
    "amcs": [...],
    "makes": [...],
    "models": [...],
    "categories": [...],
    "subCategories": [...],
    "vendors": [...],
    "outlets": [...],
    "statuses": [...],
    "summary": {
      "totalUsers": 10,
      "totalAssets": 50,
      ...
    },
    "attention": {
      "expiringWarranties": [...],
      "expiringWarrantiesCount": 5,
      "expiringAmcs": [...],
      "expiringAmcsCount": 3,
      "assetsWithoutWarranty": [...],
      "assetsWithoutWarrantyCount": 10,
      "assetsWithoutAmc": [...],
      "assetsWithoutAmcCount": 8,
      "unassignedAssets": [...],
      "unassignedAssetsCount": 12
    },
    "audit": {
      "loginUserId": 123,
      "loginUsername": "john.doe",
      "requestedAt": "2024-01-15T10:30:00",
      "requestType": "NEED_YOUR_ATTENTION"
    }
  }
}
```

**Use Cases:**
- Dashboard overview showing all entities and attention items
- Identifying items that need attention (expiring warranties, unassigned assets, etc.)
- System health monitoring
- Reporting and analytics

**Note:** The logged-in user information is automatically extracted from the Authorization token. Make sure the token contains valid userId and username claims."""
            },
            "response": []
        }
        
        userlink_folder.setdefault('item', []).append(new_request)
        print("  Added Need Your Attention request for logged-in user")
    
    return collection

if __name__ == "__main__":
    try:
        print("🔄 Adding Need Your Attention endpoint for logged-in user...")
        
        with open('Asset_Service_Consolidated.postman_collection.json', 'r') as f:
            collection = json.load(f)
        
        collection = add_need_your_attention_variant(collection)
        
        with open('Asset_Service_Consolidated.postman_collection.json', 'w') as f:
            json.dump(collection, f, indent=2)
        
        print("✅ Collection updated!")
        
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        sys.exit(1)

