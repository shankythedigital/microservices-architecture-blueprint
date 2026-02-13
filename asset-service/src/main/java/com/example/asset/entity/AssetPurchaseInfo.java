package com.example.asset.entity;

import com.example.common.converter.JpaAttributeEncryptor;
import com.example.common.jpa.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ✅ AssetPurchaseInfo Entity
 * Stores comprehensive purchase/invoice information extracted from bills and invoices.
 * Linked to AssetMaster to maintain purchase history and financial details.
 * 🔐 DPDPA Compliance: All PII data (username) is encrypted at rest.
 */
@Entity
@Table(name = "asset_purchase_info")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "asset"})
public class AssetPurchaseInfo extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_info_id")
    private Long purchaseInfoId;

    // ============================================================
    // 🔗 Asset Relationship
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AssetMaster asset;

    // ============================================================
    // 📄 Invoice/Bill Information
    // ============================================================
    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "bill_number", length = 100)
    private String billNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "bill_date")
    private LocalDate billDate;

    @Column(name = "po_number", length = 100)
    private String poNumber; // Purchase Order Number

    @Column(name = "grn_number", length = 100)
    private String grnNumber; // Goods Receipt Note

    // ============================================================
    // 💰 Financial Information
    // ============================================================
    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "taxable_amount", precision = 15, scale = 2)
    private BigDecimal taxableAmount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "cgst_amount", precision = 15, scale = 2)
    private BigDecimal cgstAmount; // Central GST

    @Column(name = "sgst_amount", precision = 15, scale = 2)
    private BigDecimal sgstAmount; // State GST

    @Column(name = "igst_amount", precision = 15, scale = 2)
    private BigDecimal igstAmount; // Integrated GST

    @Column(name = "cgst_rate", precision = 5, scale = 2)
    private BigDecimal cgstRate;

    @Column(name = "sgst_rate", precision = 5, scale = 2)
    private BigDecimal sgstRate;

    @Column(name = "igst_rate", precision = 5, scale = 2)
    private BigDecimal igstRate;

    @Column(name = "final_amount", precision = 15, scale = 2)
    private BigDecimal finalAmount; // Total after all taxes and discounts

    // ============================================================
    // 🏢 Vendor/Supplier Information
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VendorMaster vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PurchaseOutlet outlet;

    @Column(name = "vendor_gstin", length = 15)
    private String vendorGstin; // GST Identification Number

    @Column(name = "vendor_pan", length = 10)
    private String vendorPan; // PAN Number

    @Column(name = "vendor_address", length = 500)
    private String vendorAddress;

    @Column(name = "vendor_contact", length = 50)
    private String vendorContact;

    // ============================================================
    // 📦 Product Information
    // ============================================================
    @Column(name = "hsn_code", length = 20)
    private String hsnCode; // Harmonized System of Nomenclature

    @Column(name = "sac_code", length = 20)
    private String sacCode; // Service Accounting Code

    @Column(name = "sku", length = 100)
    private String sku; // Stock Keeping Unit

    @Column(name = "part_number", length = 100)
    private String partNumber;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // ============================================================
    // 💳 Payment Information
    // ============================================================
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Cash, Card, UPI, Cheque, etc.

    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // Paid, Pending, Partial

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference; // Transaction ID, Cheque Number, etc.

    // ============================================================
    // 🚚 Delivery Information
    // ============================================================
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;

    // ============================================================
    // 📝 Additional Information
    // ============================================================
    @Column(name = "currency", length = 10)
    private String currency; // INR, USD, EUR, etc.

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "terms_and_conditions", length = 2000)
    private String termsAndConditions;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "user_id")
    private Long userId;

    // 🔐 DPDPA Compliance: Encrypted PII data
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", columnDefinition = "TEXT")
    private String username;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_favourite")
    private Boolean isFavourite = false;

    @Column(name = "is_most_like")
    private Boolean isMostLike = false;

    // ============================================================
    // 🔧 Getters and Setters
    // ============================================================
    public Long getPurchaseInfoId() { return purchaseInfoId; }
    public void setPurchaseInfoId(Long purchaseInfoId) { this.purchaseInfoId = purchaseInfoId; }

    public AssetMaster getAsset() { return asset; }
    public void setAsset(AssetMaster asset) { this.asset = asset; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getGrnNumber() { return grnNumber; }
    public void setGrnNumber(String grnNumber) { this.grnNumber = grnNumber; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getCgstRate() { return cgstRate; }
    public void setCgstRate(BigDecimal cgstRate) { this.cgstRate = cgstRate; }

    public BigDecimal getSgstRate() { return sgstRate; }
    public void setSgstRate(BigDecimal sgstRate) { this.sgstRate = sgstRate; }

    public BigDecimal getIgstRate() { return igstRate; }
    public void setIgstRate(BigDecimal igstRate) { this.igstRate = igstRate; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public VendorMaster getVendor() { return vendor; }
    public void setVendor(VendorMaster vendor) { this.vendor = vendor; }

    public PurchaseOutlet getOutlet() { return outlet; }
    public void setOutlet(PurchaseOutlet outlet) { this.outlet = outlet; }

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

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; }

    public Boolean getIsMostLike() { return isMostLike; }
    public void setIsMostLike(Boolean isMostLike) { this.isMostLike = isMostLike; }
}

