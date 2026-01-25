# Documents Bulk Upload Excel Template

## 📥 Quick Start

### Option 1: Use the CSV Template (Easiest)
1. Open `documents_bulk_upload_template.csv` in Excel or Google Sheets
2. Fill in your data
3. Save as `.xlsx` format
4. Upload via API

### Option 2: Create from Scratch
1. Create a new Excel file
2. Add the header row (Row 1):
   ```
   entity_type | entity_id | file_name | file_path | doc_type
   ```
3. Add your data rows starting from Row 2
4. Save as `.xlsx` format

### Option 3: Generate with Python (Advanced)
1. Install openpyxl: `pip install openpyxl`
2. Run: `python generate_documents_template.py`
3. Use the generated `documents_bulk_upload_template.xlsx`

## 📊 Required Columns

| Column | Required | Type | Description | Example |
|--------|----------|------|-------------|---------|
| **entity_type** | ✅ Yes | Text | Entity type | `ASSET`, `COMPONENT`, `AMC`, `WARRANTY`, etc. |
| **entity_id** | ✅ Yes | Number | Entity ID | `1`, `2`, `100` |
| **file_name** | ✅ Yes | Text | File name | `invoice_001.pdf` |
| **file_path** | ✅ Yes | Text | Full server path | `/uploads/documents/invoice_001.pdf` |
| **doc_type** | ❌ No | Text | Document type | `PDF`, `IMAGE`, `RECEIPT`, `AGREEMENT` |

## 📋 Entity Types Supported

- `ASSET`
- `COMPONENT`
- `AMC`
- `WARRANTY`
- `CATEGORY`
- `SUBCATEGORY`
- `MAKE`
- `MODEL`
- `OUTLET`
- `VENDOR`

## ⚠️ Important Notes

1. **File Path Must Exist**: The `file_path` must point to a file that **already exists** on the server
2. **Entity Must Exist**: The `entity_id` must reference an existing entity in the database
3. **Header Row Required**: Always include a header row in Row 1
4. **Case Insensitive**: Column names are case-insensitive (e.g., `Entity Type` = `entity_type`)
5. **File Format**: Use `.xlsx` (Excel 2007+) format

## 📤 API Endpoint

```
POST {{assetbaseUrl}}/api/asset/v1/documents/bulk/excel
Content-Type: multipart/form-data

Parameters:
- file: Excel file (.xlsx)
- userId: 1
- username: "admin"
- projectType: "ASSET_SERVICE" (optional)
```

## 📝 Example Data

See `documents_bulk_upload_template.csv` for complete examples.

