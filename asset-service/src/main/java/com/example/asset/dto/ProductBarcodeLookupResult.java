package com.example.asset.dto;

/**
 * Result from public product barcode lookup (OpenFoodFacts, UPC Item DB).
 * Standardized format for EAN/UPC/GTIN product barcodes.
 */
public class ProductBarcodeLookupResult {

    private String barcode;
    private String productName;
    private String category;
    private String subcategory;
    private String brand;
    private String model;
    private String source;  // "OpenFoodFacts" or "UPCItemDB"

    public ProductBarcodeLookupResult() {
    }

    public ProductBarcodeLookupResult(String barcode, String productName, String category,
                                      String subcategory, String brand, String model, String source) {
        this.barcode = barcode;
        this.productName = productName;
        this.category = category;
        this.subcategory = subcategory;
        this.brand = brand;
        this.model = model;
        this.source = source;
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
