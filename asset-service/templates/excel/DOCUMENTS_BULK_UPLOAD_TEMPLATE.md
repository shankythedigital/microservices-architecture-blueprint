# Documents Bulk Upload Excel Template

## 📋 Overview

This template is used for bulk uploading documents via the Excel endpoint:
```
POST {{assetbaseUrl}}/api/asset/v1/documents/bulk/excel
```

## 📊 Excel File Format

### Required Columns

| Column Name | Type | Description | Example |
|------------|------|-------------|---------|
| **entity_type** | String | Type of entity the document is linked to | `ASSET`, `COMPONENT`, `AMC`, `WARRANTY`, `CATEGORY`, `SUBCATEGORY`, `MAKE`, `MODEL`, `OUTLET`, `VENDOR` |
| **entity_id** | Number | ID of the entity | `1`, `2`, `100` |
| **file_name** | String | Name of the file | `invoice_001.pdf`, `receipt_002.jpg` |
| **file_path** | String | Full path to the file on the server | `/uploads/documents/invoice_001.pdf` |

### Optional Columns

| Column Name | Type | Description | Example |
|------------|------|-------------|---------|
| **document_id** | Number | Primary key (for updates) | `1`, `2` |
| **doc_type** | String | Document type | `PDF`, `IMAGE`, `RECEIPT`, `AGREEMENT` |

## 📝 Column Details

### entity_type
- **Required**: Yes
- **Values**: `ASSET`, `COMPONENT`, `AMC`, `WARRANTY`, `CATEGORY`, `SUBCATEGORY`, `MAKE`, `MODEL`, `OUTLET`, `VENDOR`
- **Case**: Case-insensitive (will be converted to uppercase)
- **Description**: Specifies which type of entity this document is linked to

### entity_id
- **Required**: Yes
- **Type**: Integer/Long
- **Description**: The ID of the entity (must exist in the database)
- **Example**: `1`, `100`, `500`

### file_name
- **Required**: Yes
- **Type**: String
- **Max Length**: 255 characters
- **Description**: The name of the file
- **Example**: `invoice_001.pdf`, `receipt_002.jpg`, `manual.pdf`

### file_path
- **Required**: Yes
- **Type**: String
- **Max Length**: 500 characters
- **Description**: Full path to the file on the server where the file already exists
- **Note**: The file must already exist at this path on the server
- **Example**: `/uploads/documents/invoice_001.pdf`, `/var/files/receipts/receipt_002.jpg`

### doc_type (Optional)
- **Required**: No
- **Type**: String
- **Values**: `PDF`, `IMAGE`, `RECEIPT`, `AGREEMENT`, or any custom type
- **Description**: Type/category of the document

### document_id (Optional)
- **Required**: No
- **Type**: Integer/Long
- **Description**: Primary key for document updates (if updating existing documents)

## 📋 Excel Template Structure

### Row 1: Header Row (Required)
```
entity_type | entity_id | file_name | file_path | doc_type
```

### Row 2+: Data Rows
```
ASSET | 1 | invoice_001.pdf | /uploads/documents/invoice_001.pdf | PDF
ASSET | 2 | receipt_002.jpg | /uploads/documents/receipt_002.jpg | IMAGE
COMPONENT | 1 | manual_001.pdf | /uploads/documents/manual_001.pdf | PDF
```

## ✅ Example Excel Content

| entity_type | entity_id | file_name | file_path | doc_type |
|------------|-----------|-----------|-----------|----------|
| ASSET | 1 | invoice_001.pdf | /uploads/documents/invoice_001.pdf | PDF |
| ASSET | 2 | receipt_002.jpg | /uploads/documents/receipt_002.jpg | IMAGE |
| COMPONENT | 1 | manual_001.pdf | /uploads/documents/manual_001.pdf | PDF |
| WARRANTY | 1 | warranty_001.pdf | /uploads/documents/warranty_001.pdf | PDF |
| AMC | 1 | amc_agreement_001.pdf | /uploads/documents/amc_agreement_001.pdf | AGREEMENT |
| CATEGORY | 1 | category_image_001.jpg | /uploads/documents/category_image_001.jpg | IMAGE |
| SUBCATEGORY | 1 | subcategory_image_001.jpg | /uploads/documents/subcategory_image_001.jpg | IMAGE |
| MAKE | 1 | make_logo_001.png | /uploads/documents/make_logo_001.png | IMAGE |
| MODEL | 1 | model_spec_001.pdf | /uploads/documents/model_spec_001.pdf | PDF |
| OUTLET | 1 | outlet_photo_001.jpg | /uploads/documents/outlet_photo_001.jpg | IMAGE |
| VENDOR | 1 | vendor_contract_001.pdf | /uploads/documents/vendor_contract_001.pdf | PDF |

## 🔧 Column Name Flexibility

The parser is flexible with column names:
- **Case-insensitive**: `Entity Type`, `ENTITY_TYPE`, `entity_type` all work
- **Spaces**: `Entity Type` = `entity_type`
- **Special characters**: `entity-type` = `entity_type`
- **Normalization**: All column names are normalized to lowercase with underscores

### Accepted Column Name Variations:
- `entity_type`, `Entity Type`, `ENTITY_TYPE`, `entity-type`, `Entity_Type`
- `entity_id`, `Entity ID`, `ENTITY_ID`, `entity-id`, `Entity_Id`
- `file_name`, `File Name`, `FILE_NAME`, `file-name`, `File_Name`
- `file_path`, `File Path`, `FILE_PATH`, `file-path`, `File_Path`
- `doc_type`, `Doc Type`, `DOC_TYPE`, `doc-type`, `Doc_Type`
- `document_id`, `Document ID`, `DOCUMENT_ID`, `document-id`, `Document_Id`

## ⚠️ Important Notes

1. **File Path Requirement**: The `file_path` must point to a file that **already exists** on the server. This endpoint does NOT upload files - it only creates document records for existing files.

2. **Entity Validation**: The `entity_id` must reference an existing and active entity in the database. If the entity doesn't exist, the row will fail.

3. **File Path Validation**: The system will verify that the file exists at the specified path. If the file doesn't exist, the row will fail.

4. **Empty Rows**: Empty rows are automatically skipped.

5. **Error Handling**: If a row fails, it will be logged but processing continues for other rows.

6. **File Format**: Use `.xlsx` format (Excel 2007+) for best compatibility.

## 📤 API Request Format

```
POST {{assetbaseUrl}}/api/asset/v1/documents/bulk/excel
Content-Type: multipart/form-data

Parameters:
- file: Excel file (.xlsx or .xls)
- userId: Long (required)
- username: String (required)
- projectType: String (optional, defaults to "ASSET_SERVICE")
```

## 📥 Response Format

```json
{
    "success": true,
    "message": "Excel upload completed: 2/2 successful",
    "data": {
        "totalCount": 2,
        "successCount": 2,
        "failureCount": 0,
        "results": [
            {
                "index": 0,
                "success": true,
                "errorMessage": null,
                "item": {
                    "documentId": 1,
                    "fileName": "invoice_001.pdf",
                    "filePath": "/uploads/documents/invoice_001.pdf",
                    "entityType": "ASSET",
                    "entityId": 1,
                    "docType": "PDF"
                }
            },
            {
                "index": 1,
                "success": true,
                "errorMessage": null,
                "item": {
                    "documentId": 2,
                    "fileName": "receipt_002.jpg",
                    "filePath": "/uploads/documents/receipt_002.jpg",
                    "entityType": "ASSET",
                    "entityId": 2,
                    "docType": "IMAGE"
                }
            }
        ]
    }
}
```

## 🚀 Quick Start

1. Download the CSV template: `documents_bulk_upload_template.csv`
2. Open it in Excel or Google Sheets
3. Fill in your document data
4. Save as `.xlsx` format
5. Upload via Postman or your API client

## 📝 Tips

- **Column Order**: Column order doesn't matter - the parser uses column names
- **Header Row**: Always include a header row (Row 1)
- **Data Validation**: Ensure entity IDs exist before uploading
- **File Paths**: Verify file paths are correct and files exist on the server
- **Batch Size**: Process in batches of 100-500 rows for better performance

