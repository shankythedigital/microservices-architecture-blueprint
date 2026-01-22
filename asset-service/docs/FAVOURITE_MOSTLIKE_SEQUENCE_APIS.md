# Favourite, Most Like, and Sequence Order APIs

## Overview
Separate REST APIs have been created for managing `favourite`, `mostLike`, and `sequenceOrder` fields across all master data entities. These APIs allow users to manage their wishlist and admins to control ordering.

## Access Control
- **Favourite & Most Like**: Accessible to all authenticated users (for personal wishlist management)
- **Sequence Order**: Admin only (for governance and ordering control)

## API Endpoints Pattern

### For Categories
- `PUT /api/asset/v1/categories/{id}/favourite?isFavourite=true`
- `PUT /api/asset/v1/categories/{id}/most-like?isMostLike=true`
- `PUT /api/asset/v1/categories/{id}/sequence-order?sequenceOrder=10` (Admin only)

### For SubCategories
- `PUT /api/asset/v1/subcategories/{id}/favourite?isFavourite=true`
- `PUT /api/asset/v1/subcategories/{id}/most-like?isMostLike=true`
- `PUT /api/asset/v1/subcategories/{id}/sequence-order?sequenceOrder=10` (Admin only)

### For Models
- `PUT /api/asset/v1/models/{id}/favourite?isFavourite=true`
- `PUT /api/asset/v1/models/{id}/most-like?isMostLike=true`
- `PUT /api/asset/v1/models/{id}/sequence-order?sequenceOrder=10` (Admin only)

### For Makes
- `PUT /api/asset/v1/makes/{id}/favourite?isFavourite=true`
- `PUT /api/asset/v1/makes/{id}/most-like?isMostLike=true`
- `PUT /api/asset/v1/makes/{id}/sequence-order?sequenceOrder=10` (Admin only)

### For Statuses
- `PUT /api/asset/v1/statuses/{id}/favourite?isFavourite=true`
- `PUT /api/asset/v1/statuses/{id}/most-like?isMostLike=true`
- `PUT /api/asset/v1/statuses/{id}/sequence-order?sequenceOrder=10` (Admin only)

## Request Headers
All endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
```

## Response Format
```json
{
  "success": true,
  "message": "⭐ Category favourite updated successfully",
  "data": {
    "categoryId": 1,
    "categoryName": "Electronics",
    "isFavourite": true,
    "isMostLike": false,
    "sequenceOrder": 5
  }
}
```

## Implementation Status

### ✅ Completed
1. **Categories** - CategoryService & CategoryController
2. **SubCategories** - SubCategoryService & SubCategoryController
3. **Models** - ModelService & ModelController
4. **Makes** - MakeService & MakeController
5. **Statuses** - StatusService & StatusController

### 🔄 Remaining (To be implemented)
6. **Vendors** - VendorService & VendorController
7. **Outlets** - OutletService & OutletController
8. **Assets** - AssetCrudService & AssetController
9. **Components** - ComponentService & ComponentController
10. **Warranties** - AssetWarrantyService & AssetWarrantyController
11. **AMCs** - AssetAmcService & AssetAmcController
12. **User Links** - UserLinkService & UserLinkController

## Usage Examples

### Mark a Category as Favourite
```bash
curl -X PUT "https://api.example.com/api/asset/v1/categories/1/favourite?isFavourite=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Mark a Model as Most Like
```bash
curl -X PUT "https://api.example.com/api/asset/v1/models/5/most-like?isMostLike=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Update Sequence Order (Admin Only)
```bash
curl -X PUT "https://api.example.com/api/asset/v1/categories/1/sequence-order?sequenceOrder=10" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## Error Responses

### Unauthorized (401)
```json
{
  "success": false,
  "message": "❌ Missing Authorization header",
  "data": null
}
```

### Access Denied (403)
```json
{
  "success": false,
  "message": "Access denied: Only admins can update sequence order",
  "data": null
}
```

### Not Found (404)
```json
{
  "success": false,
  "message": "Category not found with id: 999",
  "data": null
}
```

## Notes
- All endpoints are idempotent (safe to call multiple times)
- `isFavourite` and `isMostLike` default to `true` if not specified
- `sequenceOrder` must be provided for sequence order updates
- Admin check is performed using JWT role claims (`ROLE_ADMIN` or `ADMIN`)

