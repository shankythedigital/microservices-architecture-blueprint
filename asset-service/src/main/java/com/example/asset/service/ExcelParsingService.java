package com.example.asset.service;

import com.example.asset.dto.*;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * ✅ ExcelParsingService
 * 
 * <p>Service for parsing Excel files and converting them to request DTOs for bulk upload operations.
 * This service supports parsing Excel files with column-based headers, extracting both primary keys
 * and foreign keys from the Excel data.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><b>Column Name-Based Parsing:</b> Uses header row to map column names to data, making the parser
 *       flexible and column-order independent</li>
 *   <li><b>Primary Key Support:</b> Extracts primary key values (IDs) from Excel for update operations</li>
 *   <li><b>Foreign Key Support:</b> Extracts foreign key values (IDs) from Excel for relationship mapping</li>
 *   <li><b>Flexible Column Naming:</b> Handles various column name formats (case-insensitive, spaces, special chars)</li>
 *   <li><b>Robust Error Handling:</b> Continues processing even if individual rows fail, logging warnings</li>
 * </ul>
 * 
 * <h3>Excel File Format Requirements:</h3>
 * <ul>
 *   <li>First row (row 0) must contain column headers</li>
 *   <li>Data rows start from row 1</li>
 *   <li>Column names are case-insensitive and spaces are normalized</li>
 *   <li>Primary keys and foreign keys can be numeric (Long) values</li>
 * </ul>
 * 
 * <h3>Supported Entities:</h3>
 * <ul>
 *   <li>Categories (ProductCategory)</li>
 *   <li>SubCategories (ProductSubCategory) - with foreign key to Category</li>
 *   <li>Makes (ProductMake) - with foreign key to SubCategory</li>
 *   <li>Models (ProductModel) - with foreign key to Make</li>
 *   <li>Components (AssetComponent)</li>
 *   <li>Vendors (VendorMaster)</li>
 *   <li>Outlets (PurchaseOutlet)</li>
 * </ul>
 * 
 * <h3>Excel Format Summary:</h3>
 * <p>All parsing methods support column name-based parsing with the following format:</p>
 * <ul>
 *   <li><b>Row 0:</b> Header row with column names (required)</li>
 *   <li><b>Row 1+:</b> Data rows with values</li>
 *   <li><b>Primary Keys:</b> Optional ID columns (category_id, subcategory_id, etc.)</li>
 *   <li><b>Foreign Keys:</b> Optional FK columns (category_id, sub_category_id, make_id)</li>
 * </ul>
 * 
 * <h3>Column Name Normalization:</h3>
 * <p>Column names are automatically normalized for flexible matching:</p>
 * <ul>
 *   <li>Case-insensitive: "Category Name" = "category_name" = "CATEGORY_NAME"</li>
 *   <li>Spaces converted: "Category Name" → "category_name"</li>
 *   <li>Special chars removed: "category-id" → "category_id"</li>
 * </ul>
 * 
 * @author Asset Service Team
 * @version 2.0
 * @since 1.0
 */
@Service
public class ExcelParsingService {

    private static final Logger log = LoggerFactory.getLogger(ExcelParsingService.class);

    // Repositories for entity lookups (used in legacy parsing methods)
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;

    /**
     * Constructor for ExcelParsingService.
     * 
     * @param categoryRepo Repository for ProductCategory lookups
     * @param subCategoryRepo Repository for ProductSubCategory lookups
     * @param makeRepo Repository for ProductMake lookups
     */
    public ExcelParsingService(ProductCategoryRepository categoryRepo,
                               ProductSubCategoryRepository subCategoryRepo,
                               ProductMakeRepository makeRepo) {
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
    }

    // ============================================================
    // 📦 PARSE VENDORS FROM EXCEL
    // ============================================================
    public List<VendorRequest> parseVendors(MultipartFile file, Long userId, String username, String projectType) {
        List<VendorRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file must contain at least one sheet");
            }

            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String vendorName = getCellValue(row, 0);
                    if (vendorName == null || vendorName.trim().isEmpty()) {
                        continue; // Skip empty rows
                    }

                    VendorMaster vendor = new VendorMaster();
                    vendor.setVendorName(vendorName.trim());
                    vendor.setContactPerson(getCellValue(row, 1));
                    vendor.setEmail(getCellValue(row, 2));
                    vendor.setMobile(getCellValue(row, 3));
                    vendor.setAddress(getCellValue(row, 4));

                    VendorRequest request = new VendorRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setVendor(vendor);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse vendor at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse vendors Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }

    // ============================================================
    // 📦 PARSE OUTLETS FROM EXCEL (Legacy Method - Index-based)
    // ============================================================
    /**
     * Legacy method for parsing outlets from Excel using column indices.
     * 
     * <p><b>Note:</b> This method uses column indices (0, 1, 2, etc.) instead of column names.
     * For new implementations, use parseOutletsSimple() which uses column name-based parsing.</p>
     * 
     * <h3>📊 Excel File Format (Legacy - Index-based):</h3>
     * <pre>
     * Row 0 (Header): | outlet_name | outlet_address    | contact_info      |
     * Row 1 (Data):  | Store A     | 123 Main Street    | Phone: 123-4567   |
     * Row 2 (Data):  | Store B     | 456 Oak Avenue     | Phone: 987-6543   |
     * </pre>
     * 
     * <h3>Column Mapping (by index):</h3>
     * <ul>
     *   <li>Column 0: outlet_name (required)</li>
     *   <li>Column 1: outlet_address (optional)</li>
     *   <li>Column 2: contact_info (optional)</li>
     * </ul>
     * 
     * @param file Excel file containing outlet data
     * @param userId User ID for audit trail
     * @param username Username for audit trail
     * @param projectType Project type for notifications
     * @return List of OutletRequest objects
     */
    public List<OutletRequest> parseOutlets(MultipartFile file, Long userId, String username, String projectType) {
        List<OutletRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String outletName = getCellValue(row, 0);
                    if (outletName == null || outletName.trim().isEmpty()) {
                        continue;
                    }

                    PurchaseOutlet outlet = new PurchaseOutlet();
                    outlet.setOutletName(outletName.trim());
                    outlet.setOutletAddress(getCellValue(row, 1));
                    outlet.setContactInfo(getCellValue(row, 2));

                    OutletRequest request = new OutletRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setOutlet(outlet);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse outlet at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse outlets Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }

    // ============================================================
    // 📦 PARSE CATEGORIES FROM EXCEL
    // ============================================================
    public List<CategoryRequest> parseCategories(MultipartFile file, Long userId, String username, String projectType) {
        List<CategoryRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String categoryName = getCellValue(row, 0);
                    if (categoryName == null || categoryName.trim().isEmpty()) {
                        continue;
                    }

                    ProductCategory category = new ProductCategory(categoryName.trim());
                    category.setDescription(getCellValue(row, 1));

                    CategoryRequest request = new CategoryRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setCategory(category);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse category at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse categories Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }

    // ============================================================
    // 📦 PARSE SUBCATEGORIES FROM EXCEL
    // ============================================================
    public List<SubCategoryRequest> parseSubCategories(MultipartFile file, Long userId, String username, String projectType) {
        List<SubCategoryRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String subCategoryName = getCellValue(row, 0);
                    if (subCategoryName == null || subCategoryName.trim().isEmpty()) {
                        continue;
                    }

                    String categoryName = getCellValue(row, 1);
                    ProductCategory category = null;
                    if (categoryName != null && !categoryName.trim().isEmpty()) {
                        category = categoryRepo.findByCategoryNameIgnoreCase(categoryName.trim())
                                .orElse(null);
                        if (category == null) {
                            log.warn("⚠️ Category '{}' not found for subcategory '{}' at row {}", 
                                    categoryName, subCategoryName, i + 1);
                        }
                    }

                    ProductSubCategory subCategory = new ProductSubCategory();
                    subCategory.setSubCategoryName(subCategoryName.trim());
                    subCategory.setDescription(getCellValue(row, 2));
                    subCategory.setCategory(category);

                    SubCategoryRequest request = new SubCategoryRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setSubCategory(subCategory);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse subcategory at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse subcategories Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }

    // ============================================================
    // 📦 PARSE MAKES FROM EXCEL
    // ============================================================
    public List<MakeRequest> parseMakes(MultipartFile file, Long userId, String username, String projectType) {
        List<MakeRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String makeName = getCellValue(row, 0);
                    if (makeName == null || makeName.trim().isEmpty()) {
                        continue;
                    }

                    String subCategoryName = getCellValue(row, 1);
                    ProductSubCategory subCategory = null;
                    if (subCategoryName != null && !subCategoryName.trim().isEmpty()) {
                        subCategory = subCategoryRepo.findBySubCategoryNameIgnoreCase(subCategoryName.trim())
                                .orElse(null);
                        if (subCategory == null) {
                            log.warn("⚠️ SubCategory '{}' not found for make '{}' at row {}", 
                                    subCategoryName, makeName, i + 1);
                        }
                    }

                    ProductMake make = new ProductMake();
                    make.setMakeName(makeName.trim());
                    make.setSubCategory(subCategory);

                    MakeRequest request = new MakeRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setMake(make);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse make at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse makes Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }

    // ============================================================
    // 📦 PARSE MODELS FROM EXCEL
    // ============================================================
    public List<ModelRequest> parseModels(MultipartFile file, Long userId, String username, String projectType) {
        List<ModelRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String modelName = getCellValue(row, 0);
                    if (modelName == null || modelName.trim().isEmpty()) {
                        continue;
                    }

                    String makeName = getCellValue(row, 1);
                    ProductMake make = null;
                    if (makeName != null && !makeName.trim().isEmpty()) {
                        make = makeRepo.findByMakeNameIgnoreCase(makeName.trim())
                                .orElse(null);
                        if (make == null) {
                            log.warn("⚠️ Make '{}' not found for model '{}' at row {}", 
                                    makeName, modelName, i + 1);
                        }
                    }

                    ProductModel model = new ProductModel();
                    model.setModelName(modelName.trim());
                    model.setDescription(getCellValue(row, 2));
                    model.setMake(make);

                    ModelRequest request = new ModelRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setModel(model);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse model at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse models Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }

    // ============================================================
    // 📦 PARSE COMPONENTS FROM EXCEL (Legacy Method - Index-based)
    // ============================================================
    /**
     * Legacy method for parsing components from Excel using column indices.
     * 
     * <p><b>Note:</b> This method uses column indices (0, 1, etc.) instead of column names.
     * For new implementations, use parseComponentsSimple() which uses column name-based parsing.</p>
     * 
     * <h3>📊 Excel File Format (Legacy - Index-based):</h3>
     * <pre>
     * Row 0 (Header): | component_name | description      |
     * Row 1 (Data):  | RAM            | Memory module    |
     * Row 2 (Data):  | SSD            | Storage drive    |
     * </pre>
     * 
     * <h3>Column Mapping (by index):</h3>
     * <ul>
     *   <li>Column 0: component_name (required)</li>
     *   <li>Column 1: description (optional)</li>
     * </ul>
     * 
     * @param file Excel file containing component data
     * @param userId User ID for audit trail
     * @param username Username for audit trail
     * @param projectType Project type for notifications
     * @return List of ComponentRequest objects
     */
    public List<ComponentRequest> parseComponents(MultipartFile file, Long userId, String username, String projectType) {
        List<ComponentRequest> requests = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String componentName = getCellValue(row, 0);
                    if (componentName == null || componentName.trim().isEmpty()) {
                        continue;
                    }

                    AssetComponent component = new AssetComponent();
                    component.setComponentName(componentName.trim());
                    component.setDescription(getCellValue(row, 1));

                    ComponentRequest request = new ComponentRequest();
                    request.setUserId(userId);
                    request.setUsername(username);
                    request.setProjectType(projectType);
                    request.setComponent(component);

                    requests.add(request);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to parse component at row {}: {}", i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse components Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return requests;
    }
// ============================================================
// 🆕 NEW SIMPLE CATEGORY PARSER FOR BulkCategoryRequest
// ============================================================
/**
 * Parses categories from an Excel file using column name-based mapping.
 * 
 * <p>This method reads the Excel file and extracts category data based on column headers.
 * It supports both primary keys and flexible column ordering.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | category_id | category_name | description |
 * Row 1 (Data):  | 1           | Electronics   | Electronic items |
 * Row 2 (Data):  | 2           | Furniture     | Furniture items |
 * Row 3 (Data):  |             | Appliances    | Home appliances |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>category_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>category_name</td><td>String</td><td>Yes</td><td>Name of the category</td></tr>
 *   <tr><td>description</td><td>String</td><td>No</td><td>Description of the category</td></tr>
 * </table>
 * 
 * <h3>Column Name Variations Supported:</h3>
 * <ul>
 *   <li>"category_id", "Category ID", "CATEGORY_ID", "Category_Id"</li>
 *   <li>"category_name", "Category Name", "CATEGORY_NAME"</li>
 *   <li>"description", "Description", "DESCRIPTION"</li>
 * </ul>
 * 
 * <h3>Processing Logic:</h3>
 * <ol>
 *   <li>Reads header row (row 0) to build column name to index mapping</li>
 *   <li>Processes each data row starting from row 1</li>
 *   <li>Extracts primary key (category_id) if present</li>
 *   <li>Extracts category_name (required field)</li>
 *   <li>Extracts description (optional field)</li>
 *   <li>Skips rows with empty category_name</li>
 *   <li>Continues processing even if individual rows fail (logs warning)</li>
 * </ol>
 * 
 * @param file The Excel file (MultipartFile) containing category data
 * @return List of SimpleCategoryDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkCategoryRequest.SimpleCategoryDto
 */
public List<BulkCategoryRequest.SimpleCategoryDto> parseCategoriesSimple(MultipartFile file) {
    List<BulkCategoryRequest.SimpleCategoryDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        // Read header row (row 0) to build column map
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        
        log.info("📋 Parsing categories with columns: {}", columnMap.keySet());

        // Process data rows starting from row 1
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                // Get category_name (required)
                String categoryName = getCellValueByColumnName(row, columnMap, "category_name");
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    log.debug("⏭️ Skipping row {}: category_name is empty", i + 1);
                    continue; // skip blank rows
                }

                // Get description (required)
                String description = getCellValueByColumnName(row, columnMap, "description");
                if (description == null || description.trim().isEmpty()) {
                    log.debug("⏭️ Skipping row {}: description is empty", i + 1);
                    continue; // skip blank rows
                }
                
                // Get category_id (optional - primary key from Excel)
                Long categoryId = getLongValueByColumnName(row, columnMap, "category_id");

                BulkCategoryRequest.SimpleCategoryDto dto =
                        new BulkCategoryRequest.SimpleCategoryDto();
                dto.setCategoryId(categoryId); // Set primary key from Excel
                dto.setCategoryName(categoryName.trim());
                dto.setDescription(description != null ? description.trim() : null);
                
                if (categoryId != null) {
                    log.debug("📝 Row {} has category_id: {}", i + 1, categoryId);
                }

                list.add(dto);

            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse category row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} categories from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse categories Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse categories Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE SUBCATEGORY PARSER FOR BulkSubCategoryRequest
// ============================================================
/**
 * Parses subcategories from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts subcategory data including primary keys and foreign keys.
 * SubCategories have a relationship to Categories via category_id foreign key.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | subcategory_id | subcategory_name | description | category_id | category_name |
 * Row 1 (Data):  | 1              | Laptops         | Laptop items | 1          | Electronics   |
 * Row 2 (Data):  | 2              | Desks           | Desk items   | 2          | Furniture     |
 * Row 3 (Data):  |                | Chairs          | Chair items  |            | Furniture     |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>subcategory_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>subcategory_name</td><td>String</td><td>Yes</td><td>Name of the subcategory</td></tr>
 *   <tr><td>description</td><td>String</td><td>No</td><td>Description of the subcategory</td></tr>
 *   <tr><td>category_id</td><td>Long</td><td>No</td><td>Foreign key to ProductCategory</td></tr>
 *   <tr><td>category_name</td><td>String</td><td>No</td><td>Category name for lookup (alternative to category_id)</td></tr>
 * </table>
 * 
 * <h3>Foreign Key Handling:</h3>
 * <p>The parser extracts vendor_id (foreign key) from Excel.
 * Services can use the ID directly to set the vendor relationship.</p>
 * 
 * <h3>Relationship:</h3>
 * <p>PurchaseOutlet → VendorMaster (Many-to-One via vendor_id)</p>
 * 
 * @param file The Excel file (MultipartFile) containing subcategory data
 * @return List of SimpleSubCategoryDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkSubCategoryRequest.SimpleSubCategoryDto
 */
public List<BulkSubCategoryRequest.SimpleSubCategoryDto> parseSubCategoriesSimple(MultipartFile file) {
    List<BulkSubCategoryRequest.SimpleSubCategoryDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing subcategories with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String subCategoryName = getCellValueByColumnName(row, columnMap, "subcategory_name");
                if (subCategoryName == null || subCategoryName.trim().isEmpty()) {
                    continue;
                }

                // Get subcategory_id (optional - primary key from Excel)
                Long subCategoryId = getLongValueByColumnName(row, columnMap, "subcategory_id");
                
                // Get category_id (optional - foreign key from Excel)
                Long categoryId = getLongValueByColumnName(row, columnMap, "category_id");

                BulkSubCategoryRequest.SimpleSubCategoryDto dto =
                        new BulkSubCategoryRequest.SimpleSubCategoryDto();
                dto.setSubCategoryId(subCategoryId); // Set primary key from Excel
                dto.setCategoryId(categoryId); // Set foreign key from Excel
                dto.setSubCategoryName(subCategoryName.trim());
                dto.setDescription(getCellValueByColumnName(row, columnMap, "description"));
                dto.setCategoryName(getCellValueByColumnName(row, columnMap, "category_name"));

                list.add(dto);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse subcategory row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} subcategories from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse subcategories Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse subcategories Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE MAKE PARSER FOR BulkMakeRequest
// ============================================================
/**
 * Parses makes from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts make data including primary keys and foreign keys.
 * Makes have a relationship to SubCategories via sub_category_id foreign key.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | make_id | make_name | sub_category_id | subcategory_name |
 * Row 1 (Data):  | 1       | Dell      | 1               | Laptops          |
 * Row 2 (Data):  | 2       | HP        | 1               | Laptops          |
 * Row 3 (Data):  |         | IKEA      | 2               | Desks            |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>make_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>make_name</td><td>String</td><td>Yes</td><td>Name of the make</td></tr>
 *   <tr><td>sub_category_id</td><td>Long</td><td>No</td><td>Foreign key to ProductSubCategory</td></tr>
 *   <tr><td>subcategory_name</td><td>String</td><td>No</td><td>SubCategory name for lookup (alternative to sub_category_id)</td></tr>
 * </table>
 * 
 * <h3>Foreign Key Handling:</h3>
 * <p>The parser extracts both sub_category_id (foreign key) and subcategory_name (for lookup).
 * Services can use either the ID directly or perform a lookup by name.</p>
 * 
 * <h3>Relationship:</h3>
 * <p>ProductMake → ProductSubCategory (Many-to-One via sub_category_id)</p>
 * 
 * @param file The Excel file (MultipartFile) containing make data
 * @return List of SimpleMakeDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkMakeRequest.SimpleMakeDto
 */
public List<BulkMakeRequest.SimpleMakeDto> parseMakesSimple(MultipartFile file) {
    List<BulkMakeRequest.SimpleMakeDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing makes with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String makeName = getCellValueByColumnName(row, columnMap, "make_name");
                if (makeName == null || makeName.trim().isEmpty()) {
                    continue;
                }

                // Get make_id (optional - primary key from Excel)
                Long makeId = getLongValueByColumnName(row, columnMap, "make_id");
                
                // Get sub_category_id (optional - foreign key from Excel)
                Long subCategoryId = getLongValueByColumnName(row, columnMap, "sub_category_id");

                BulkMakeRequest.SimpleMakeDto dto = new BulkMakeRequest.SimpleMakeDto();
                dto.setMakeId(makeId); // Set primary key from Excel
                dto.setSubCategoryId(subCategoryId); // Set foreign key from Excel
                dto.setMakeName(makeName.trim());
                dto.setSubCategoryName(getCellValueByColumnName(row, columnMap, "subcategory_name"));

                list.add(dto);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse make row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} makes from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse makes Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse makes Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE MODEL PARSER FOR BulkModelRequest
// ============================================================
/**
 * Parses models from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts model data including primary keys and foreign keys.
 * Models have a relationship to Makes via make_id foreign key.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | model_id | model_name | description | make_id | make_name |
 * Row 1 (Data):  | 1        | XPS 13     | Dell XPS 13 | 1       | Dell      |
 * Row 2 (Data):  | 2        | Pavilion   | HP Pavilion | 2       | HP        |
 * Row 3 (Data):  |          | ThinkPad   | Lenovo ThinkPad |      | Lenovo   |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>model_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>model_name</td><td>String</td><td>Yes</td><td>Name of the model</td></tr>
 *   <tr><td>description</td><td>String</td><td>No</td><td>Description of the model</td></tr>
 *   <tr><td>make_id</td><td>Long</td><td>No</td><td>Foreign key to ProductMake</td></tr>
 *   <tr><td>make_name</td><td>String</td><td>No</td><td>Make name for lookup (alternative to make_id)</td></tr>
 * </table>
 * 
 * <h3>Foreign Key Handling:</h3>
 * <p>The parser extracts both make_id (foreign key) and make_name (for lookup).
 * Services can use either the ID directly or perform a lookup by name.</p>
 * 
 * <h3>Relationship:</h3>
 * <p>ProductModel → ProductMake (Many-to-One via make_id)</p>
 * 
 * @param file The Excel file (MultipartFile) containing model data
 * @return List of SimpleModelDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkModelRequest.SimpleModelDto
 */
public List<BulkModelRequest.SimpleModelDto> parseModelsSimple(MultipartFile file) {
    List<BulkModelRequest.SimpleModelDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing models with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String modelName = getCellValueByColumnName(row, columnMap, "model_name");
                if (modelName == null || modelName.trim().isEmpty()) {
                    continue;
                }

                // Get model_id (optional - primary key from Excel)
                Long modelId = getLongValueByColumnName(row, columnMap, "model_id");
                
                // Get make_id (optional - foreign key from Excel)
                Long makeId = getLongValueByColumnName(row, columnMap, "make_id");

                BulkModelRequest.SimpleModelDto dto = new BulkModelRequest.SimpleModelDto();
                dto.setModelId(modelId); // Set primary key from Excel
                dto.setMakeId(makeId); // Set foreign key from Excel
                dto.setModelName(modelName.trim());
                dto.setDescription(getCellValueByColumnName(row, columnMap, "description"));
                dto.setMakeName(getCellValueByColumnName(row, columnMap, "make_name"));

                list.add(dto);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse model row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} models from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse models Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse models Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE COMPONENT PARSER FOR BulkComponentRequest
// ============================================================
/**
 * Parses components from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts component data including primary keys.
 * Components are standalone entities without foreign key relationships.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | component_id | component_name | description |
 * Row 1 (Data):  | 1            | RAM            | Memory module |
 * Row 2 (Data):  | 2            | SSD            | Storage drive |
 * Row 3 (Data):  |              | Battery        | Laptop battery |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>component_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>component_name</td><td>String</td><td>Yes</td><td>Name of the component</td></tr>
 *   <tr><td>description</td><td>String</td><td>No</td><td>Description of the component</td></tr>
 * </table>
 * 
 * @param file The Excel file (MultipartFile) containing component data
 * @return List of SimpleComponentDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkComponentRequest.SimpleComponentDto
 */
public List<BulkComponentRequest.SimpleComponentDto> parseComponentsSimple(MultipartFile file) {
    List<BulkComponentRequest.SimpleComponentDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing components with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String componentName = getCellValueByColumnName(row, columnMap, "component_name");
                if (componentName == null || componentName.trim().isEmpty()) {
                    continue;
                }

                // Get component_id (optional - primary key from Excel)
                Long componentId = getLongValueByColumnName(row, columnMap, "component_id");

                BulkComponentRequest.SimpleComponentDto dto =
                        new BulkComponentRequest.SimpleComponentDto();
                dto.setComponentId(componentId); // Set primary key from Excel
                dto.setComponentName(componentName.trim());
                dto.setDescription(getCellValueByColumnName(row, columnMap, "description"));

                list.add(dto);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse component row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} components from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse components Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse components Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE VENDOR PARSER FOR BulkVendorRequest
// ============================================================
/**
 * Parses vendors from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts vendor data including primary keys.
 * Vendors are standalone entities without foreign key relationships.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | vendor_id | vendor_name | contact_person | email              | mobile      | address           |
 * Row 1 (Data):  | 1         | ABC Corp    | John Doe       | john@abccorp.com  | 1234567890  | 123 Main St       |
 * Row 2 (Data):  | 2         | XYZ Ltd     | Jane Smith     | jane@xyzltd.com   | 9876543210  | 456 Oak Ave       |
 * Row 3 (Data):  |           | Tech Inc    | Bob Johnson    | bob@techinc.com   | 5555555555  | 789 Pine Rd       |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>vendor_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>vendor_name</td><td>String</td><td>Yes</td><td>Name of the vendor</td></tr>
 *   <tr><td>contact_person</td><td>String</td><td>No</td><td>Contact person name</td></tr>
 *   <tr><td>email</td><td>String</td><td>No</td><td>Vendor email address</td></tr>
 *   <tr><td>mobile</td><td>String</td><td>No</td><td>Vendor mobile number</td></tr>
 *   <tr><td>address</td><td>String</td><td>No</td><td>Vendor address</td></tr>
 * </table>
 * 
 * @param file The Excel file (MultipartFile) containing vendor data
 * @return List of SimpleVendorDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkVendorRequest.SimpleVendorDto
 */
public List<BulkVendorRequest.SimpleVendorDto> parseVendorsSimple(MultipartFile file) {
    List<BulkVendorRequest.SimpleVendorDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing vendors with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String vendorName = getCellValueByColumnName(row, columnMap, "vendor_name");
                if (vendorName == null || vendorName.trim().isEmpty()) {
                    continue;
                }

                // Get vendor_id (optional - primary key from Excel)
                Long vendorId = getLongValueByColumnName(row, columnMap, "vendor_id");

                BulkVendorRequest.SimpleVendorDto dto = new BulkVendorRequest.SimpleVendorDto();
                dto.setVendorId(vendorId); // Set primary key from Excel
                dto.setVendorName(vendorName.trim());
                dto.setContactPerson(getCellValueByColumnName(row, columnMap, "contact_person"));
                dto.setEmail(getCellValueByColumnName(row, columnMap, "email"));
                dto.setMobile(getCellValueByColumnName(row, columnMap, "mobile"));
                dto.setAddress(getCellValueByColumnName(row, columnMap, "address"));

                list.add(dto);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse vendor row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} vendors from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse vendors Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse vendors Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE OUTLET PARSER FOR BulkOutletRequest
// ============================================================
/**
 * Parses outlets from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts outlet data including primary keys and foreign keys.
 * Outlets can have an optional relationship to VendorMaster via vendor_id.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | outlet_id | outlet_name | outlet_address    | contact_info      | vendor_id |
 * Row 1 (Data):  | 1         | Store A     | 123 Main Street    | Phone: 123-4567   | 1        |
 * Row 2 (Data):  | 2         | Store B     | 456 Oak Avenue    | Phone: 987-6543   | 1        |
 * Row 3 (Data):  |           | Store C     | 789 Pine Road     | Email: store@c.com | 2        |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>outlet_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>outlet_name</td><td>String</td><td>Yes</td><td>Name of the outlet</td></tr>
 *   <tr><td>outlet_address</td><td>String</td><td>No</td><td>Outlet address</td></tr>
 *   <tr><td>contact_info</td><td>String</td><td>No</td><td>Contact information</td></tr>
 *   <tr><td>vendor_id</td><td>Long</td><td>No</td><td>Foreign key to VendorMaster</td></tr>
 * </table>
 * 
 * <h3>Foreign Key Handling:</h3>
 * <p>The parser extracts vendor_id (foreign key) from Excel.
 * Services can use the ID directly to set the vendor relationship.</p>
 * 
 * <h3>Relationship:</h3>
 * <p>PurchaseOutlet → VendorMaster (Many-to-One via vendor_id)</p>
 * 
 * @param file The Excel file (MultipartFile) containing outlet data
 * @return List of SimpleOutletDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkOutletRequest.SimpleOutletDto
 */
public List<BulkOutletRequest.SimpleOutletDto> parseOutletsSimple(MultipartFile file) {
    List<BulkOutletRequest.SimpleOutletDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing outlets with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String outletName = getCellValueByColumnName(row, columnMap, "outlet_name");
                if (outletName == null || outletName.trim().isEmpty()) {
                    continue;
                }

                // Get outlet_id (optional - primary key from Excel)
                Long outletId = getLongValueByColumnName(row, columnMap, "outlet_id");
                
                // Get vendor_id (optional - foreign key from Excel)
                Long vendorId = getLongValueByColumnName(row, columnMap, "vendor_id");

                BulkOutletRequest.SimpleOutletDto dto = new BulkOutletRequest.SimpleOutletDto();
                dto.setOutletId(outletId); // Set primary key from Excel
                dto.setVendorId(vendorId); // Set foreign key from Excel
                dto.setOutletName(outletName.trim());
                dto.setOutletAddress(getCellValueByColumnName(row, columnMap, "outlet_address"));
                dto.setContactInfo(getCellValueByColumnName(row, columnMap, "contact_info"));

                list.add(dto);
            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse outlet row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} outlets from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse outlets Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse outlets Excel: " + e.getMessage(), e);
    }

    return list;
}

// ============================================================
// 🆕 NEW SIMPLE ASSET PARSER FOR BulkAssetRequest
// ============================================================
/**
 * Parses assets from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts asset data including primary keys and foreign keys.
 * Assets have relationships to Category, SubCategory, Make, Model, and Components.</p>
 * 
 * <p><b>IMPORTANT:</b> Components are stored in separate rows. Multiple rows with the same
 * asset_name_udv (or asset_id) will be grouped together, and all component_ids from those rows
 * will be collected for that asset.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | asset_id | asset_name_udv | asset_status | category_id | category_name | sub_category_id | subcategory_name | make_id | make_name | model_id | model_name | component_id | component_name |
 * Row 1 (Data):  | 1        | LAPTOP-001     | AVAILABLE    | 1           | Electronics   | 1               | Laptops          | 1       | Dell      | 1        | XPS 13     | 1            | RAM            |
 * Row 2 (Data):  | 1        | LAPTOP-001     | AVAILABLE    | 1           | Electronics   | 1               | Laptops          | 1       | Dell      | 1        | XPS 13     | 2            | SSD            |
 * Row 3 (Data):  | 2        | LAPTOP-002     | IN_USE       | 1           | Electronics   | 1               | Laptops          | 2       | HP        | 2        | Pavilion   | 2            | SSD            |
 * Row 4 (Data):  | 2        | LAPTOP-002     | IN_USE       | 1           | Electronics   | 1               | Laptops          | 2       | HP        | 2        | Pavilion   | 3            | HDD            |
 * Row 5 (Data):  |          | DESKTOP-001    | AVAILABLE    | 2           | Furniture     | 2               | Desks            |         |           |          |             |              |                |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>asset_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>asset_name_udv</td><td>String</td><td>Yes</td><td>Unique asset name/identifier (used for grouping rows)</td></tr>
 *   <tr><td>asset_status</td><td>String</td><td>No</td><td>Asset status (e.g., AVAILABLE, IN_USE)</td></tr>
 *   <tr><td>category_id</td><td>Long</td><td>No</td><td>Foreign key to ProductCategory (prioritized over category_name)</td></tr>
 *   <tr><td>category_name</td><td>String</td><td>No</td><td>Category name for lookup (alternative to category_id)</td></tr>
 *   <tr><td>sub_category_id</td><td>Long</td><td>No</td><td>Foreign key to ProductSubCategory (prioritized over subcategory_name)</td></tr>
 *   <tr><td>subcategory_name</td><td>String</td><td>No</td><td>SubCategory name for lookup (alternative to sub_category_id)</td></tr>
 *   <tr><td>make_id</td><td>Long</td><td>No</td><td>Foreign key to ProductMake (prioritized over make_name)</td></tr>
 *   <tr><td>make_name</td><td>String</td><td>No</td><td>Make name for lookup (alternative to make_id)</td></tr>
 *   <tr><td>model_id</td><td>Long</td><td>No</td><td>Foreign key to ProductModel (prioritized over model_name)</td></tr>
 *   <tr><td>model_name</td><td>String</td><td>No</td><td>Model name for lookup (alternative to model_id)</td></tr>
 *   <tr><td>component_id</td><td>Long</td><td>No</td><td>Component ID (one per row - multiple rows with same asset_name_udv will be grouped)</td></tr>
 *   <tr><td>component_name</td><td>String</td><td>No</td><td>Component name (alternative to component_id, one per row)</td></tr>
 * </table>
 * 
 * <h3>Row Grouping Logic:</h3>
 * <p>Rows are grouped by asset_name_udv (or asset_id if provided). All rows with the same
 * asset identifier will be merged into a single asset DTO with all components collected.</p>
 * <ul>
 *   <li>If asset_id is provided, it's used as the grouping key</li>
 *   <li>Otherwise, asset_name_udv is used as the grouping key</li>
 *   <li>Asset details (category, make, model, etc.) are taken from the first row of each group</li>
 *   <li>All component_ids and component_names from all rows in the group are collected</li>
 * </ul>
 * 
 * <h3>Foreign Key Handling:</h3>
 * <p>The parser extracts foreign keys (IDs) and names from Excel.
 * Services prioritize IDs over names for lookups.</p>
 * 
 * <h3>Component Handling:</h3>
 * <p>Each row can have one component_id or component_name. Multiple rows with the same
 * asset_name_udv will have their components collected into a list.</p>
 * 
 * <h3>Relationships:</h3>
 * <ul>
 *   <li>AssetMaster → ProductCategory (Many-to-One via category_id)</li>
 *   <li>AssetMaster → ProductSubCategory (Many-to-One via sub_category_id)</li>
 *   <li>AssetMaster → ProductMake (Many-to-One via make_id)</li>
 *   <li>AssetMaster → ProductModel (Many-to-One via model_id)</li>
 *   <li>AssetMaster → AssetComponent (Many-to-Many via asset_component_link)</li>
 * </ul>
 * 
 * @param file The Excel file (MultipartFile) containing asset data
 * @return List of SimpleAssetDto objects parsed from Excel (one per unique asset, with all components collected)
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkAssetRequest.SimpleAssetDto
 */
public List<BulkAssetRequest.SimpleAssetDto> parseAssetsSimple(MultipartFile file) {
    // Map to group rows by asset identifier (asset_id or asset_name_udv)
    Map<String, BulkAssetRequest.SimpleAssetDto> assetMap = new LinkedHashMap<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing assets with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                // Get asset_name_udv (required)
                String assetNameUdv = getCellValueByColumnName(row, columnMap, "asset_name_udv");
                if (assetNameUdv == null || assetNameUdv.trim().isEmpty()) {
                    log.debug("⏭️ Skipping row {}: asset_name_udv is empty", i + 1);
                    continue; // skip blank rows
                }

                assetNameUdv = assetNameUdv.trim();
                
                // Get asset_id (optional - primary key from Excel)
                Long assetId = getLongValueByColumnName(row, columnMap, "asset_id");
                
                // Determine grouping key: use asset_id if available, otherwise use asset_name_udv
                String groupKey = assetId != null ? "ID:" + assetId : "NAME:" + assetNameUdv;
                
                // Get or create asset DTO for this group
                BulkAssetRequest.SimpleAssetDto dto = assetMap.get(groupKey);
                if (dto == null) {
                    // First row for this asset - create new DTO
                    dto = new BulkAssetRequest.SimpleAssetDto();
                    dto.setAssetId(assetId);
                    dto.setAssetNameUdv(assetNameUdv);
                    
                    // Get asset_status (optional)
                    String assetStatus = getCellValueByColumnName(row, columnMap, "asset_status");
                    dto.setAssetStatus(assetStatus != null ? assetStatus.trim() : null);
                    
                    // Get category_id (optional - foreign key from Excel, prioritized)
                    Long categoryId = getLongValueByColumnName(row, columnMap, "category_id");
                    dto.setCategoryId(categoryId);
                    
                    // Get category_name (optional - for lookup if category_id not provided)
                    String categoryName = getCellValueByColumnName(row, columnMap, "category_name");
                    dto.setCategoryName(categoryName != null ? categoryName.trim() : null);
                    
                    // Get sub_category_id (optional - foreign key from Excel, prioritized)
                    Long subCategoryId = getLongValueByColumnName(row, columnMap, "sub_category_id");
                    dto.setSubCategoryId(subCategoryId);
                    
                    // Get subcategory_name (optional - for lookup if sub_category_id not provided)
                    String subCategoryName = getCellValueByColumnName(row, columnMap, "subcategory_name");
                    dto.setSubCategoryName(subCategoryName != null ? subCategoryName.trim() : null);
                    
                    // Get make_id (optional - foreign key from Excel, prioritized)
                    Long makeId = getLongValueByColumnName(row, columnMap, "make_id");
                    dto.setMakeId(makeId);
                    
                    // Get make_name (optional - for lookup if make_id not provided)
                    String makeName = getCellValueByColumnName(row, columnMap, "make_name");
                    dto.setMakeName(makeName != null ? makeName.trim() : null);
                    
                    // Get model_id (optional - foreign key from Excel, prioritized)
                    Long modelId = getLongValueByColumnName(row, columnMap, "model_id");
                    dto.setModelId(modelId);
                    
                    // Get model_name (optional - for lookup if model_id not provided)
                    String modelName = getCellValueByColumnName(row, columnMap, "model_name");
                    dto.setModelName(modelName != null ? modelName.trim() : null);
                    
                    // Initialize component lists
                    dto.setComponentIds(new ArrayList<>());
                    dto.setComponentNames(new ArrayList<>());
                    
                    assetMap.put(groupKey, dto);
                }
                
                // Collect component from this row (one component per row)
                Long componentId = getLongValueByColumnName(row, columnMap, "component_id");
                if (componentId != null) {
                    if (dto.getComponentIds() == null) {
                        dto.setComponentIds(new ArrayList<>());
                    }
                    dto.getComponentIds().add(componentId);
                }
                
                String componentName = getCellValueByColumnName(row, columnMap, "component_name");
                if (componentName != null && !componentName.trim().isEmpty()) {
                    if (dto.getComponentNames() == null) {
                        dto.setComponentNames(new ArrayList<>());
                    }
                    dto.getComponentNames().add(componentName.trim());
                }

            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse asset row {}: {}", i + 1, ex.getMessage());
            }
        }

        List<BulkAssetRequest.SimpleAssetDto> result = new ArrayList<>(assetMap.values());
        log.info("✅ Parsed {} unique assets from Excel (grouped {} rows)", result.size(), sheet.getLastRowNum());

    } catch (Exception e) {
        log.error("❌ Failed to parse assets Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse assets Excel: " + e.getMessage(), e);
    }

    return new ArrayList<>(assetMap.values());
}

// ============================================================
// 🆕 NEW SIMPLE DOCUMENT PARSER FOR BulkDocumentRequest
// ============================================================
/**
 * Parses documents from an Excel file using column name-based mapping.
 * 
 * <p>This method extracts document metadata including entity linkage information.
 * Documents can be linked to various entities: ASSET, COMPONENT, AMC, WARRANTY,
 * CATEGORY, SUBCATEGORY, MAKE, MODEL, OUTLET, VENDOR.</p>
 * 
 * <h3>📊 Excel File Format:</h3>
 * <pre>
 * Row 0 (Header): | document_id | entity_type | entity_id | file_name | file_path | doc_type |
 * Row 1 (Data):  | 1           | ASSET       | 1         | image.jpg | /uploads/ASSET/image.jpg | IMAGE |
 * Row 2 (Data):  | 2           | ASSET       | 1         | receipt.pdf | /uploads/ASSET/receipt.pdf | PDF |
 * Row 3 (Data):  | 3           | CATEGORY    | 2         | category.png | /uploads/CATEGORY/category.png | IMAGE |
 * Row 4 (Data):  |             | VENDOR      | 5         | vendor_logo.jpg | /uploads/VENDOR/vendor_logo.jpg | IMAGE |
 * </pre>
 * 
 * <h3>Expected Excel Columns:</h3>
 * <table border="1">
 *   <tr><th>Column Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
 *   <tr><td>document_id</td><td>Long</td><td>No</td><td>Primary key (for updates)</td></tr>
 *   <tr><td>entity_type</td><td>String</td><td>Yes</td><td>Entity type: ASSET, COMPONENT, AMC, WARRANTY, CATEGORY, SUBCATEGORY, MAKE, MODEL, OUTLET, VENDOR</td></tr>
 *   <tr><td>entity_id</td><td>Long</td><td>Yes</td><td>ID of the entity this document is linked to</td></tr>
 *   <tr><td>file_name</td><td>String</td><td>Yes</td><td>Name of the file (e.g., image.jpg, document.pdf)</td></tr>
 *   <tr><td>file_path</td><td>String</td><td>Yes</td><td>Path to the file (can be relative or absolute)</td></tr>
 *   <tr><td>doc_type</td><td>String</td><td>No</td><td>Document type (e.g., IMAGE, PDF, RECEIPT, AGREEMENT)</td></tr>
 * </table>
 * 
 * <h3>Entity Type Validation:</h3>
 * <p>The parser accepts the following entity types (case-insensitive):
 * <ul>
 *   <li>ASSET - Links to AssetMaster</li>
 *   <li>COMPONENT - Links to AssetComponent</li>
 *   <li>AMC - Links to AssetAmc</li>
 *   <li>WARRANTY - Links to AssetWarranty</li>
 *   <li>CATEGORY - Links to ProductCategory</li>
 *   <li>SUBCATEGORY - Links to ProductSubCategory</li>
 *   <li>MAKE - Links to ProductMake</li>
 *   <li>MODEL - Links to ProductModel</li>
 *   <li>OUTLET - Links to PurchaseOutlet</li>
 *   <li>VENDOR - Links to VendorMaster</li>
 * </ul>
 * </p>
 * 
 * <h3>File Path Handling:</h3>
 * <p>The file_path can be:
 * <ul>
 *   <li>Absolute path: /var/uploads/ASSET/image.jpg</li>
 *   <li>Relative path: uploads/ASSET/image.jpg</li>
 *   <li>The service will validate that the file exists at the specified path</li>
 * </ul>
 * </p>
 * 
 * <h3>Processing Logic:</h3>
 * <ol>
 *   <li>Reads header row (row 0) to build column name to index mapping</li>
 *   <li>Processes each data row starting from row 1</li>
 *   <li>Extracts primary key (document_id) if present</li>
 *   <li>Extracts entity_type and entity_id (required fields)</li>
 *   <li>Extracts file_name and file_path (required fields)</li>
 *   <li>Extracts doc_type (optional field)</li>
 *   <li>Skips rows with missing required fields</li>
 *   <li>Continues processing even if individual rows fail (logs warning)</li>
 * </ol>
 * 
 * @param file The Excel file (MultipartFile) containing document metadata
 * @return List of SimpleDocumentDto objects parsed from Excel
 * @throws RuntimeException if file parsing fails or file structure is invalid
 * 
 * @see BulkDocumentRequest.SimpleDocumentDto
 */
public List<BulkDocumentRequest.SimpleDocumentDto> parseDocumentsSimple(MultipartFile file) {
    List<BulkDocumentRequest.SimpleDocumentDto> list = new ArrayList<>();

    try (InputStream is = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 0) {
            throw new IllegalArgumentException("Excel file must contain at least one sheet with data");
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel file must have a header row");
        }
        
        Map<String, Integer> columnMap = buildColumnMap(headerRow);
        log.info("📋 Parsing documents with columns: {}", columnMap.keySet());

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                // Get entity_type (required)
                String entityType = getCellValueByColumnName(row, columnMap, "entity_type");
                if (entityType == null || entityType.trim().isEmpty()) {
                    log.debug("⏭️ Skipping row {}: entity_type is empty", i + 1);
                    continue; // skip blank rows
                }

                // Get entity_id (required)
                Long entityId = getLongValueByColumnName(row, columnMap, "entity_id");
                if (entityId == null) {
                    log.debug("⏭️ Skipping row {}: entity_id is empty", i + 1);
                    continue; // skip rows without entity_id
                }

                // Get file_name (required)
                String fileName = getCellValueByColumnName(row, columnMap, "file_name");
                if (fileName == null || fileName.trim().isEmpty()) {
                    log.debug("⏭️ Skipping row {}: file_name is empty", i + 1);
                    continue; // skip rows without file_name
                }

                // Get file_path (required)
                String filePath = getCellValueByColumnName(row, columnMap, "file_path");
                if (filePath == null || filePath.trim().isEmpty()) {
                    log.debug("⏭️ Skipping row {}: file_path is empty", i + 1);
                    continue; // skip rows without file_path
                }

                BulkDocumentRequest.SimpleDocumentDto dto = new BulkDocumentRequest.SimpleDocumentDto();
                
                // Get document_id (optional - primary key from Excel)
                Long documentId = getLongValueByColumnName(row, columnMap, "document_id");
                dto.setDocumentId(documentId);
                
                dto.setEntityType(entityType.trim().toUpperCase());
                dto.setEntityId(entityId);
                dto.setFileName(fileName.trim());
                dto.setFilePath(filePath.trim());
                
                // Get doc_type (optional)
                String docType = getCellValueByColumnName(row, columnMap, "doc_type");
                dto.setDocType(docType != null ? docType.trim() : null);

                list.add(dto);
                
                if (documentId != null) {
                    log.debug("📝 Row {} has document_id: {}", i + 1, documentId);
                }

            } catch (Exception ex) {
                log.warn("⚠️ Failed to parse document row {}: {}", i + 1, ex.getMessage());
            }
        }

        log.info("✅ Parsed {} documents from Excel", list.size());

    } catch (Exception e) {
        log.error("❌ Failed to parse documents Excel: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to parse documents Excel: " + e.getMessage(), e);
    }

    return list;
}

    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    
    /**
     * Builds a map of column names to column indices from the header row.
     * 
     * <p>This method reads the header row (first row) of the Excel sheet and creates a mapping
     * between column names and their indices. Column names are normalized to handle various
     * formats and make the parser flexible.</p>
     * 
     * <h3>Normalization Process:</h3>
     * <ol>
     *   <li>Trims whitespace from column names</li>
     *   <li>Converts to lowercase</li>
     *   <li>Replaces spaces with underscores</li>
     *   <li>Removes special characters (keeps only alphanumeric and underscores)</li>
     * </ol>
     * 
     * <h3>Example Normalizations:</h3>
     * <ul>
     *   <li>"Category Name" → "category_name"</li>
     *   <li>"category-id" → "category_id"</li>
     *   <li>"Category_ID" → "category_id"</li>
     * </ul>
     * 
     * <h3>Dual Mapping:</h3>
     * <p>The method creates two mappings for each column:
     * <ul>
     *   <li>Normalized name → index (e.g., "category_name" → 0)</li>
     *   <li>Original lowercase → index (e.g., "category name" → 0)</li>
     * </ul>
     * This ensures maximum flexibility in column name matching.</p>
     * 
     * @param headerRow The first row (row 0) containing column headers
     * @return Map of normalized column name to column index (0-based)
     */
    private Map<String, Integer> buildColumnMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        if (headerRow == null) {
            return columnMap;
        }

        for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = getCellValue(headerRow, i);
                if (columnName != null && !columnName.trim().isEmpty()) {
                    // Normalize column name: lowercase, trim, replace spaces with underscores
                    String normalized = columnName.trim()
                            .toLowerCase()
                            .replaceAll("\\s+", "_")
                            .replaceAll("[^a-z0-9_]", "");
                    columnMap.put(normalized, i);
                    // Also add original case-insensitive match for flexibility
                    columnMap.put(columnName.trim().toLowerCase(), i);
                }
            }
        }
        
        log.debug("📋 Column map built: {}", columnMap);
        return columnMap;
    }

    /**
     * Gets cell value by column name from a row using the column map.
     * 
     * <p>This method retrieves a cell value from a data row by looking up the column name
     * in the column map. The column name is normalized before lookup to match the format
     * used in buildColumnMap().</p>
     * 
     * <h3>Lookup Process:</h3>
     * <ol>
     *   <li>Normalizes the requested column name</li>
     *   <li>Looks up the column index in the column map</li>
     *   <li>If not found, tries direct lowercase lookup</li>
     *   <li>Retrieves the cell value using the column index</li>
     *   <li>Returns null if column not found or cell is empty</li>
     * </ol>
     * 
     * <h3>Error Handling:</h3>
     * <p>If the column is not found, a warning is logged with available column names
     * to help users identify the correct column name format.</p>
     * 
     * @param row The data row to extract value from
     * @param columnMap Map of column names to indices (from buildColumnMap)
     * @param columnName The column name to look up (will be normalized automatically)
     * @return Cell value as string, or null if column not found or cell is empty
     */
    private String getCellValueByColumnName(Row row, Map<String, Integer> columnMap, String columnName) {
        if (row == null || columnMap == null || columnName == null) {
            return null;
        }
        
        // Normalize column name
        String normalized = columnName.trim()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");
        
        Integer columnIndex = columnMap.get(normalized);
        if (columnIndex == null) {
            // Try direct lookup
            columnIndex = columnMap.get(columnName.trim().toLowerCase());
        }
        
        if (columnIndex == null) {
            log.warn("⚠️ Column '{}' not found in header. Available columns: {}", 
                    columnName, columnMap.keySet());
            return null;
        }
        
        return getCellValue(row, columnIndex);
    }

    /**
     * Gets Long value by column name from a row using the column map.
     * 
     * <p>This method is specifically designed for parsing primary key and foreign key values
     * from Excel cells. It handles numeric values stored in Excel (which are often stored
     * as doubles) and converts them to Long values.</p>
     * 
     * <h3>Use Cases:</h3>
     * <ul>
     *   <li>Parsing primary keys (category_id, subcategory_id, make_id, etc.)</li>
     *   <li>Parsing foreign keys (category_id, sub_category_id, make_id, etc.)</li>
     * </ul>
     * 
     * <h3>Conversion Process:</h3>
     * <ol>
     *   <li>Gets the cell value as string using getCellValueByColumnName()</li>
     *   <li>Parses the string as a double (Excel numbers are often doubles)</li>
     *   <li>Converts double to long (truncates decimal part)</li>
     *   <li>Returns null if value is empty or parsing fails</li>
     * </ol>
     * 
     * <h3>Example:</h3>
     * <ul>
     *   <li>Excel cell value: 123.0 (stored as double) → Returns: 123L</li>
     *   <li>Excel cell value: "123" (stored as string) → Returns: 123L</li>
     *   <li>Excel cell value: "" (empty) → Returns: null</li>
     * </ul>
     * 
     * @param row The data row to extract value from
     * @param columnMap Map of column names to indices (from buildColumnMap)
     * @param columnName The column name to look up (e.g., "category_id", "make_id")
     * @return Long value parsed from the cell, or null if not found or invalid
     */
    private Long getLongValueByColumnName(Row row, Map<String, Integer> columnMap, String columnName) {
        String valueStr = getCellValueByColumnName(row, columnMap, columnName);
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing as double first (Excel numbers are often stored as doubles)
            double doubleValue = Double.parseDouble(valueStr.trim());
            long longValue = (long) doubleValue;
            return longValue;
        } catch (NumberFormatException e) {
            log.warn("⚠️ Could not parse '{}' as Long for column '{}': {}", valueStr, columnName, e.getMessage());
            return null;
        }
    }

    /**
     * Gets cell value by column index, converting it to a string representation.
     * 
     * <p>This is a low-level method that extracts the raw value from an Excel cell
     * and converts it to a string. It handles various cell types including strings,
     * numbers, dates, booleans, and formulas.</p>
     * 
     * <h3>Cell Type Handling:</h3>
     * <ul>
     *   <li><b>STRING:</b> Returns the string value directly</li>
     *   <li><b>NUMERIC:</b> 
     *       <ul>
     *         <li>If date-formatted: Returns date as string</li>
     *         <li>If whole number: Returns without decimal (e.g., 123.0 → "123")</li>
     *         <li>If decimal: Returns with decimal (e.g., 123.45 → "123.45")</li>
     *       </ul>
     *   </li>
     *   <li><b>BOOLEAN:</b> Returns "true" or "false" as string</li>
     *   <li><b>FORMULA:</b> Returns the formula text (not the calculated value)</li>
     *   <li><b>Other types:</b> Returns null</li>
     * </ul>
     * 
     * <h3>Note:</h3>
     * <p>For formula cells, this method returns the formula text, not the calculated value.
     * If you need the calculated value, you would need to evaluate the formula separately.</p>
     * 
     * @param row The Excel row containing the cell
     * @param cellIndex The zero-based column index
     * @return String representation of the cell value, or null if cell is empty or invalid
     */
    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert to string without decimal if it's a whole number
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}

