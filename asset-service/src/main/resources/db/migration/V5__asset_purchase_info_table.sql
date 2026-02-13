-- ============================================================
-- ✅ Asset Purchase Info Table
-- Stores comprehensive purchase/invoice information extracted from bills
-- ============================================================

CREATE TABLE IF NOT EXISTS asset_purchase_info (
    purchase_info_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Asset Relationship
    asset_id BIGINT NOT NULL,
    FOREIGN KEY (asset_id) REFERENCES asset_master(asset_id),
    
    -- Invoice/Bill Information
    invoice_number VARCHAR(100),
    bill_number VARCHAR(100),
    invoice_date DATE,
    bill_date DATE,
    po_number VARCHAR(100),
    grn_number VARCHAR(100),
    
    -- Financial Information
    purchase_price DECIMAL(15, 2),
    unit_price DECIMAL(15, 2),
    quantity INT,
    total_amount DECIMAL(15, 2),
    discount_amount DECIMAL(15, 2),
    discount_percentage DECIMAL(5, 2),
    taxable_amount DECIMAL(15, 2),
    tax_amount DECIMAL(15, 2),
    tax_rate DECIMAL(5, 2),
    cgst_amount DECIMAL(15, 2),
    sgst_amount DECIMAL(15, 2),
    igst_amount DECIMAL(15, 2),
    cgst_rate DECIMAL(5, 2),
    sgst_rate DECIMAL(5, 2),
    igst_rate DECIMAL(5, 2),
    final_amount DECIMAL(15, 2),
    
    -- Vendor/Supplier Information
    vendor_id BIGINT,
    outlet_id BIGINT,
    vendor_gstin VARCHAR(15),
    vendor_pan VARCHAR(10),
    vendor_address VARCHAR(500),
    vendor_contact VARCHAR(50),
    FOREIGN KEY (vendor_id) REFERENCES vendor_master(vendor_id),
    FOREIGN KEY (outlet_id) REFERENCES purchase_outlet(outlet_id),
    
    -- Product Information
    hsn_code VARCHAR(20),
    sac_code VARCHAR(20),
    sku VARCHAR(100),
    part_number VARCHAR(100),
    batch_number VARCHAR(100),
    manufacturing_date DATE,
    expiry_date DATE,
    
    -- Payment Information
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    payment_date DATE,
    due_date DATE,
    payment_terms VARCHAR(200),
    payment_reference VARCHAR(100),
    
    -- Delivery Information
    delivery_date DATE,
    delivery_address VARCHAR(500),
    delivery_status VARCHAR(50),
    
    -- Additional Information
    currency VARCHAR(10),
    exchange_rate DECIMAL(10, 4),
    terms_and_conditions VARCHAR(2000),
    notes VARCHAR(1000),
    
    -- User Context
    user_id BIGINT,
    username VARCHAR(100),
    sequence_order INT,
    is_favourite BOOLEAN DEFAULT FALSE,
    is_most_like BOOLEAN DEFAULT FALSE,
    
    -- Audit Fields (from BaseEntity)
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_asset_id (asset_id),
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_bill_number (bill_number),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_outlet_id (outlet_id),
    INDEX idx_invoice_date (invoice_date)
);

