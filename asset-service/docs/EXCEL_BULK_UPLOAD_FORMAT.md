# Excel Bulk Upload Format Guide

This document describes the Excel file format required for bulk uploading master data to the Asset Management System.

## 📋 Supported Master Endpoints

All master endpoints support Excel bulk upload via the `/bulk/excel` endpoint:

- **Vendors**: `/api/asset/v1/vendors/bulk/excel`
- **Outlets**: `/api/asset/v1/outlets/bulk/excel`
- **Categories**: `/api/asset/v1/categories/bulk/excel`
- **SubCategories**: `/api/asset/v1/subcategories/bulk/excel`
- **Makes**: `/api/asset/v1/makes/bulk/excel`
- **Models**: `/api/asset/v1/models/bulk/excel`
- **Components**: `/api/asset/v1/components/bulk/excel`

## 📤 API Request Format

All Excel upload endpoints use `multipart/form-data` with the following parameters:

- `file` (required): Excel file (.xlsx or .xls)
- `userId` (required): User ID performing the upload
- `username` (required): Username performing the upload
- `projectType` (optional): Project type (defaults to "ASSET_SERVICE")

**Example using cURL:**
```bash
curl -X POST "http://localhost:8083/api/asset/v1/vendors/bulk/excel" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@vendors.xlsx" \
  -F "userId=1" \
  -F "username=admin" \
  -F "projectType=ASSET_SERVICE"
```

## 📊 Excel File Formats

### 1. Vendors (`vendors.xlsx`)

**Sheet Structure:**
- **Row 1 (Header)**: Column names (optional, will be skipped)
- **Row 2+**: Data rows

**Column Format:**
| Column A | Column B | Column C | Column D | Column E |
|----------|----------|----------|----------|----------|
| Vendor Name* | Contact Person | Email | Mobile | Address |

**Example:**
```
Vendor Name          | Contact Person | Email              | Mobile      | Address
---------------------|----------------|--------------------|-------------|------------------
ABC Suppliers        | John Doe       | john@abc.com       | 1234567890  | 123 Main St
XYZ Corporation      | Jane Smith     | jane@xyz.com       | 9876543210  | 456 Oak Ave
```

**Notes:**
- Vendor Name is **required** (Column A)
- All other fields are optional
- Empty rows will be skipped

---

### 2. Outlets (`outlets.xlsx`)

**Column Format:**
| Column A | Column B | Column C |
|----------|----------|----------|
| Outlet Name* | Outlet Address | Contact Info |

**Example:**
```
Outlet Name        | Outlet Address        | Contact Info
-------------------|-----------------------|------------------
Amazon Online      | Online Portal         | support@amazon.in
Croma Store        | Khar West Store       | 022-11112222
Reliance Digital   | Andheri East          | 022-44443333
```

**Notes:**
- Outlet Name is **required** (Column A)
- All other fields are optional

---

### 3. Categories (`categories.xlsx`)

**Column Format:**
| Column A | Column B |
|----------|----------|
| Category Name* | Description |

**Example:**
```
Category Name        | Description
---------------------|--------------------------
Electronics          | Electronic devices and gadgets
Home Appliances      | Household appliances
Smart Home Devices   | IoT and smart home products
```

**Notes:**
- Category Name is **required** (Column A)
- Description is optional

---

### 4. SubCategories (`subcategories.xlsx`)

**Column Format:**
| Column A | Column B | Column C |
|----------|----------|----------|
| SubCategory Name* | Category Name | Description |

**Example:**
```
SubCategory Name  | Category Name    | Description
------------------|------------------|------------------
Smartphones       | Electronics      | Mobile phones
Smart TVs         | Electronics      | Television sets
Refrigerators     | Home Appliances  | Cooling appliances
Washing Machines  | Home Appliances  | Laundry appliances
```

**Notes:**
- SubCategory Name is **required** (Column A)
- Category Name should match an existing category (case-insensitive)
- If category is not found, a warning will be logged but the subcategory will still be created
- Description is optional

---

### 5. Makes (`makes.xlsx`)

**Column Format:**
| Column A | Column B |
|----------|----------|
| Make Name* | SubCategory Name |

**Example:**
```
Make Name | SubCategory Name
----------|------------------
Samsung   | Smart TVs
LG        | Refrigerators
Apple     | Smartphones
```

**Notes:**
- Make Name is **required** (Column A)
- SubCategory Name should match an existing subcategory (case-insensitive)
- If subcategory is not found, a warning will be logged but the make will still be created

---

### 6. Models (`models.xlsx`)

**Column Format:**
| Column A | Column B | Column C |
|----------|----------|----------|
| Model Name* | Make Name | Description |

**Example:**
```
Model Name        | Make Name | Description
------------------|-----------|--------------------------
iPhone 15 Pro     | Apple     | Latest iPhone model
Samsung QLED 65   | Samsung   | 65-inch QLED TV
LG InstaView 260L | LG        | 260L refrigerator
```

**Notes:**
- Model Name is **required** (Column A)
- Make Name should match an existing make (case-insensitive)
- If make is not found, a warning will be logged but the model will still be created
- Description is optional

---

### 7. Components (`components.xlsx`)

**Column Format:**
| Column A | Column B |
|----------|----------|
| Component Name* | Description |

**Example:**
```
Component Name  | Description
----------------|--------------------------
Battery Pack    | Device rechargeable battery unit
Charger         | Device adapter or charging cable
Remote Control  | TV or AC remote controller
```

**Notes:**
- Component Name is **required** (Column A)
- Description is optional

---

## ✅ Best Practices

1. **File Format**: Use `.xlsx` format (Excel 2007+) for best compatibility
2. **Header Row**: Include a header row with column names (it will be automatically skipped)
3. **Data Validation**: Ensure required fields are not empty
4. **Case Sensitivity**: Category, SubCategory, and Make names are matched case-insensitively
5. **Empty Rows**: Empty rows will be automatically skipped
6. **Error Handling**: The system processes each row individually and reports success/failure for each item

## 📥 Response Format

All Excel upload endpoints return a `BulkUploadResponse` with:

```json
{
  "success": true,
  "message": "Excel upload completed: 5/7 successful",
  "data": {
    "totalCount": 7,
    "successCount": 5,
    "failureCount": 2,
    "results": [
      {
        "index": 0,
        "success": true,
        "errorMessage": null,
        "item": { ... }
      },
      {
        "index": 1,
        "success": false,
        "errorMessage": "Vendor with name 'ABC Suppliers' already exists",
        "item": null
      },
      ...
    ]
  }
}
```

## ⚠️ Common Errors

1. **File Format Error**: Ensure the file is `.xlsx` or `.xls` format
2. **Empty File**: File must contain at least one data row
3. **Missing Required Fields**: Required columns (marked with *) must have values
4. **Duplicate Names**: Some entities require unique names (e.g., Vendor Name, Outlet Name)
5. **Invalid References**: Category/SubCategory/Make names must exist in the system

## 🔄 Processing Flow

1. Excel file is uploaded via multipart/form-data
2. File is validated (format, size, etc.)
3. Excel file is parsed row by row
4. Each row is converted to a request DTO
5. Bulk upload service processes all requests
6. Results are aggregated and returned with success/failure details

## 📝 Notes

- Maximum file size: 20MB
- Supported formats: `.xlsx`, `.xls`
- The system will continue processing even if some rows fail
- All successful items are saved, failed items are reported in the response
