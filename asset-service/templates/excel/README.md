# Excel Bulk Upload Templates

This directory contains Excel template files for bulk uploading master data.

## 📋 Available Templates

1. **vendors_template.xlsx** - For bulk uploading vendors
2. **outlets_template.xlsx** - For bulk uploading purchase outlets
3. **categories_template.xlsx** - For bulk uploading product categories
4. **subcategories_template.xlsx** - For bulk uploading product subcategories
5. **makes_template.xlsx** - For bulk uploading product makes
6. **models_template.xlsx** - For bulk uploading product models
7. **components_template.xlsx** - For bulk uploading asset components

## 🔧 Generating Templates

To generate these templates, run:

```bash
cd asset-service/tools
python3 generate_excel_templates.py
```

**Requirements:**
- Python 3.6+
- openpyxl library: `pip install openpyxl`

## 📝 Usage

1. Download or generate the template file for the master data you want to upload
2. Open the template in Excel
3. Remove the sample data rows (keep the header row)
4. Fill in your data following the column format
5. Save the file
6. Upload via the `/bulk/excel` endpoint

## 📊 Column Formats

See `docs/EXCEL_BULK_UPLOAD_FORMAT.md` for detailed column specifications.

## ⚠️ Important Notes

- **Required fields** are marked with `*` in the header row
- **Empty rows** will be automatically skipped
- **Header row** is required and will be automatically skipped during processing
- Ensure **referenced entities exist** (e.g., Category for SubCategory, Make for Model)
- File format must be `.xlsx` or `.xls`
