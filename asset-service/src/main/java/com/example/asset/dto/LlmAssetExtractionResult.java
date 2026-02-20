package com.example.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * JSON result of LLM-based asset data extraction from a document.
 * Returned by the agentic extraction endpoint for display and optional persistence.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlmAssetExtractionResult {

    private String documentType;
    private String extractedTextPreview;
    private Long processingTimeMs;

    // Asset core
    private String assetName;
    private String serialNumber;
    private String categoryName;
    private String subCategoryName;
    private String makeName;
    private String modelName;
    private String brand;
    private String description;
    private String assetStatus;

    // Purchase / invoice
    private String purchaseDate;
    private String purchasePrice;
    private String invoiceNumber;
    private String invoiceDate;
    private String billNumber;
    private String billDate;
    private String poNumber;
    private String quantity;
    private String unitPrice;
    private String totalAmount;
    private String currency;
    private String vendorName;
    private String outletName;
    private String vendorGstin;
    private String paymentMethod;
    private String paymentStatus;

    // Warranty & AMC
    private WarrantyBlock warranty;
    private AmcBlock amc;

    // Components / specs
    private List<String> componentNames;

    private String extractionMethod = "LLM_AGENT";
    private Double confidence;

    // Nested blocks for JSON
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WarrantyBlock {
        private String warrantyStatus;
        private String warrantyProvider;
        private String startDate;
        private String endDate;
        private String duration;
        private String terms;

        public String getWarrantyStatus() { return warrantyStatus; }
        public void setWarrantyStatus(String warrantyStatus) { this.warrantyStatus = warrantyStatus; }
        public String getWarrantyProvider() { return warrantyProvider; }
        public void setWarrantyProvider(String warrantyProvider) { this.warrantyProvider = warrantyProvider; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getTerms() { return terms; }
        public void setTerms(String terms) { this.terms = terms; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AmcBlock {
        private String amcStatus;
        private String provider;
        private String startDate;
        private String endDate;
        private String duration;

        public String getAmcStatus() { return amcStatus; }
        public void setAmcStatus(String amcStatus) { this.amcStatus = amcStatus; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    // Getters and setters for main type
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getExtractedTextPreview() { return extractedTextPreview; }
    public void setExtractedTextPreview(String extractedTextPreview) { this.extractedTextPreview = extractedTextPreview; }
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssetStatus() { return assetStatus; }
    public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }
    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(String purchasePrice) { this.purchasePrice = purchasePrice; }
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
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getUnitPrice() { return unitPrice; }
    public void setUnitPrice(String unitPrice) { this.unitPrice = unitPrice; }
    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getOutletName() { return outletName; }
    public void setOutletName(String outletName) { this.outletName = outletName; }
    public String getVendorGstin() { return vendorGstin; }
    public void setVendorGstin(String vendorGstin) { this.vendorGstin = vendorGstin; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public WarrantyBlock getWarranty() { return warranty; }
    public void setWarranty(WarrantyBlock warranty) { this.warranty = warranty; }
    public AmcBlock getAmc() { return amc; }
    public void setAmc(AmcBlock amc) { this.amc = amc; }
    public List<String> getComponentNames() { return componentNames; }
    public void setComponentNames(List<String> componentNames) { this.componentNames = componentNames; }
    public String getExtractionMethod() { return extractionMethod; }
    public void setExtractionMethod(String extractionMethod) { this.extractionMethod = extractionMethod; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
