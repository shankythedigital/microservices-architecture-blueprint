package com.example.asset.dto;

import com.example.asset.entity.*;
import com.example.asset.entity.AssetPurchaseInfo;

import java.util.List;

/**
 * ✅ IntelligentExtractionResponse
 * Comprehensive response DTO for intelligent document extraction.
 * Contains all extracted asset information including category, subcategory, make, model,
 * components, warranty, AMC, and more.
 */
public class IntelligentExtractionResponse {
    
    private String extractedText;
    private String documentType;
    private ExtractedAssetInfo assetInfo;
    private ProductCategory category;
    private ProductSubCategory subCategory;
    private ProductMake make;
    private ProductModel model;
    private List<AssetComponent> components;
    private AssetWarranty warranty;
    private AssetAmc amc;
    private AssetMaster asset;
    private AssetDocument document;
    private VendorMaster vendor;
    private PurchaseOutlet outlet;
    private AssetPurchaseInfo purchaseInfo;
    
    private Boolean categoryCreated;
    private Boolean subCategoryCreated;
    private Boolean makeCreated;
    private Boolean modelCreated;
    private Boolean assetCreated;
    private Boolean warrantyCreated;
    private Boolean amcCreated;
    private Boolean vendorCreated;
    private Boolean outletCreated;
    
    private String status;
    private String message;
    private Double confidence;
    private ExtractionMetadata metadata;

    // ============================================================
    // ✅ Nested class for extracted asset information
    // ============================================================
    public static class ExtractedAssetInfo {
        private String assetName;
        private String serialNumber;
        private String categoryName;
        private String subCategoryName;
        private String makeName;
        private String modelName;
        private String brand;
        private String manufacturer;
        private String description;
        private String purchaseDate;
        private String purchasePrice;
        private String vendorName;
        private String outletName;
        private String invoiceNumber;
        private String invoiceDate;
        private String billNumber;
        private String billDate;
        private String poNumber;
        private String grnNumber;
        private String quantity;
        private String unitPrice;
        private String discountAmount;
        private String discountPercentage;
        private String taxAmount;
        private String taxRate;
        private String cgstAmount;
        private String sgstAmount;
        private String igstAmount;
        private String cgstRate;
        private String sgstRate;
        private String igstRate;
        private String finalAmount;
        private String vendorGstin;
        private String vendorPan;
        private String vendorAddress;
        private String vendorContact;
        private String hsnCode;
        private String sacCode;
        private String sku;
        private String partNumber;
        private String batchNumber;
        private String paymentMethod;
        private String paymentStatus;
        private String paymentDate;
        private String dueDate;
        private String paymentTerms;
        private String paymentReference;
        private String deliveryDate;
        private String deliveryAddress;
        private String currency;
        private List<String> componentNames;
        private WarrantyInfo warrantyInfo;
        private AmcInfo amcInfo;
        private String assetStatus;

        // Getters and Setters
        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public String getSubCategoryName() { return subCategoryName; }
        public void setSubCategoryName(String subCategoryName) { this.subCategoryName = subCategoryName; }
        
        public String getMakeName() { return makeName; }
        public void setMakeName(String makeName) { this.makeName = makeName; }
        
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
        
        public String getPurchasePrice() { return purchasePrice; }
        public void setPurchasePrice(String purchasePrice) { this.purchasePrice = purchasePrice; }
        
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        
        public String getOutletName() { return outletName; }
        public void setOutletName(String outletName) { this.outletName = outletName; }
        
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        
        public String getInvoiceDate() { return invoiceDate; }
        public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
        
        public String getBillNumber() { return billNumber; }
        public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
        
        public String getBillDate() { return billDate; }
        public void setBillDate(String billDate) { this.billDate = billDate; }
        
        public String getPoNumber() { return poNumber; }
        public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
        
        public String getGrnNumber() { return grnNumber; }
        public void setGrnNumber(String grnNumber) { this.grnNumber = grnNumber; }
        
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        
        public String getUnitPrice() { return unitPrice; }
        public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }
        
        public String getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(String discountAmount) { this.discountAmount = discountAmount; }
        
        public String getDiscountPercentage() { return discountPercentage; }
        public void setDiscountPercentage(String discountPercentage) { this.discountPercentage = discountPercentage; }
        
        public String getTaxAmount() { return taxAmount; }
        public void setTaxAmount(String taxAmount) { this.taxAmount = taxAmount; }
        
        public String getTaxRate() { return taxRate; }
        public void setTaxRate(String taxRate) { this.taxRate = taxRate; }
        
        public String getCgstAmount() { return cgstAmount; }
        public void setCgstAmount(String cgstAmount) { this.cgstAmount = cgstAmount; }
        
        public String getSgstAmount() { return sgstAmount; }
        public void setSgstAmount(String sgstAmount) { this.sgstAmount = sgstAmount; }
        
        public String getIgstAmount() { return igstAmount; }
        public void setIgstAmount(String igstAmount) { this.igstAmount = igstAmount; }
        
        public String getCgstRate() { return cgstRate; }
        public void setCgstRate(String cgstRate) { this.cgstRate = cgstRate; }
        
        public String getSgstRate() { return sgstRate; }
        public void setSgstRate(String sgstRate) { this.sgstRate = sgstRate; }
        
        public String getIgstRate() { return igstRate; }
        public void setIgstRate(String igstRate) { this.igstRate = igstRate; }
        
        public String getFinalAmount() { return finalAmount; }
        public void setFinalAmount(String finalAmount) { this.finalAmount = finalAmount; }
        
        public String getVendorGstin() { return vendorGstin; }
        public void setVendorGstin(String vendorGstin) { this.vendorGstin = vendorGstin; }
        
        public String getVendorPan() { return vendorPan; }
        public void setVendorPan(String vendorPan) { this.vendorPan = vendorPan; }
        
        public String getVendorAddress() { return vendorAddress; }
        public void setVendorAddress(String vendorAddress) { this.vendorAddress = vendorAddress; }
        
        public String getVendorContact() { return vendorContact; }
        public void setVendorContact(String vendorContact) { this.vendorContact = vendorContact; }
        
        public String getHsnCode() { return hsnCode; }
        public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }
        
        public String getSacCode() { return sacCode; }
        public void setSacCode(String sacCode) { this.sacCode = sacCode; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public String getPartNumber() { return partNumber; }
        public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
        
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        
        public String getPaymentDate() { return paymentDate; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
        
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        
        public String getPaymentTerms() { return paymentTerms; }
        public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
        
        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
        
        public String getDeliveryDate() { return deliveryDate; }
        public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }
        
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public List<String> getComponentNames() { return componentNames; }
        public void setComponentNames(List<String> componentNames) { this.componentNames = componentNames; }
        
        public WarrantyInfo getWarrantyInfo() { return warrantyInfo; }
        public void setWarrantyInfo(WarrantyInfo warrantyInfo) { this.warrantyInfo = warrantyInfo; }
        
        public AmcInfo getAmcInfo() { return amcInfo; }
        public void setAmcInfo(AmcInfo amcInfo) { this.amcInfo = amcInfo; }
        
        public String getAssetStatus() { return assetStatus; }
        public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }
    }

    public static class WarrantyInfo {
        private String warrantyStatus;
        private String warrantyProvider;
        private String warrantyTerms;
        private String startDate;
        private String endDate;
        private String duration;

        // Getters and Setters
        public String getWarrantyStatus() { return warrantyStatus; }
        public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
        
        public String getWarrantyProvider() { return warrantyProvider; }
        public void setWarrantyProvider(String warrantyProvider) { this.warrantyProvider = warrantyProvider; }
        
        public String getWarrantyTerms() { return warrantyTerms; }
        public void setWarrantyTerms(String warrantyTerms) { this.warrantyTerms = warrantyTerms; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    public static class AmcInfo {
        private String amcStatus;
        private String startDate;
        private String endDate;
        private String duration;
        private String provider;

        // Getters and Setters
        public String getAmcStatus() { return amcStatus; }
        public void setAmcStatus(String amcStatus) { this.amcStatus = amcStatus; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
    }

    public static class ExtractionMetadata {
        private String documentFormat;
        private Integer pageCount;
        private String extractionMethod;
        private Long processingTimeMs;
        private List<String> detectedPatterns;

        // Getters and Setters
        public String getDocumentFormat() { return documentFormat; }
        public void setDocumentFormat(String documentFormat) { this.documentFormat = documentFormat; }
        
        public Integer getPageCount() { return pageCount; }
        public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
        
        public String getExtractionMethod() { return extractionMethod; }
        public void setExtractionMethod(String extractionMethod) { this.extractionMethod = extractionMethod; }
        
        public Long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
        
        public List<String> getDetectedPatterns() { return detectedPatterns; }
        public void setDetectedPatterns(List<String> detectedPatterns) { this.detectedPatterns = detectedPatterns; }
    }

    // ============================================================
    // ✅ Constructors
    // ============================================================
    public IntelligentExtractionResponse() {
        this.assetInfo = new ExtractedAssetInfo();
        this.metadata = new ExtractionMetadata();
    }

    // ============================================================
    // ✅ Getters and Setters
    // ============================================================
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    
    public ExtractedAssetInfo getAssetInfo() { return assetInfo; }
    public void setAssetInfo(ExtractedAssetInfo assetInfo) { this.assetInfo = assetInfo; }
    
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    
    public ProductSubCategory getSubCategory() { return subCategory; }
    public void setSubCategory(ProductSubCategory subCategory) { this.subCategory = subCategory; }
    
    public ProductMake getMake() { return make; }
    public void setMake(ProductMake make) { this.make = make; }
    
    public ProductModel getModel() { return model; }
    public void setModel(ProductModel model) { this.model = model; }
    
    public List<AssetComponent> getComponents() { return components; }
    public void setComponents(List<AssetComponent> components) { this.components = components; }
    
    public AssetWarranty getWarranty() { return warranty; }
    public void setWarranty(AssetWarranty warranty) { this.warranty = warranty; }
    
    public AssetAmc getAmc() { return amc; }
    public void setAmc(AssetAmc amc) { this.amc = amc; }
    
    public AssetMaster getAsset() { return asset; }
    public void setAsset(AssetMaster asset) { this.asset = asset; }
    
    public AssetDocument getDocument() { return document; }
    public void setDocument(AssetDocument document) { this.document = document; }
    
    public VendorMaster getVendor() { return vendor; }
    public void setVendor(VendorMaster vendor) { this.vendor = vendor; }
    
    public PurchaseOutlet getOutlet() { return outlet; }
    public void setOutlet(PurchaseOutlet outlet) { this.outlet = outlet; }
    
    public AssetPurchaseInfo getPurchaseInfo() { return purchaseInfo; }
    public void setPurchaseInfo(AssetPurchaseInfo purchaseInfo) { this.purchaseInfo = purchaseInfo; }
    
    public Boolean getCategoryCreated() { return categoryCreated; }
    public void setCategoryCreated(Boolean categoryCreated) { this.categoryCreated = categoryCreated; }
    
    public Boolean getSubCategoryCreated() { return subCategoryCreated; }
    public void setSubCategoryCreated(Boolean subCategoryCreated) { this.subCategoryCreated = subCategoryCreated; }
    
    public Boolean getMakeCreated() { return makeCreated; }
    public void setMakeCreated(Boolean makeCreated) { this.makeCreated = makeCreated; }
    
    public Boolean getModelCreated() { return modelCreated; }
    public void setModelCreated(Boolean modelCreated) { this.modelCreated = modelCreated; }
    
    public Boolean getAssetCreated() { return assetCreated; }
    public void setAssetCreated(Boolean assetCreated) { this.assetCreated = assetCreated; }
    
    public Boolean getWarrantyCreated() { return warrantyCreated; }
    public void setWarrantyCreated(Boolean warrantyCreated) { this.warrantyCreated = warrantyCreated; }
    
    public Boolean getAmcCreated() { return amcCreated; }
    public void setAmcCreated(Boolean amcCreated) { this.amcCreated = amcCreated; }
    
    public Boolean getVendorCreated() { return vendorCreated; }
    public void setVendorCreated(Boolean vendorCreated) { this.vendorCreated = vendorCreated; }
    
    public Boolean getOutletCreated() { return outletCreated; }
    public void setOutletCreated(Boolean outletCreated) { this.outletCreated = outletCreated; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public ExtractionMetadata getMetadata() { return metadata; }
    public void setMetadata(ExtractionMetadata metadata) { this.metadata = metadata; }
}

