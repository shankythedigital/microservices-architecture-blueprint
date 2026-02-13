package com.example.asset.repository;

import com.example.asset.entity.AssetPurchaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetPurchaseInfoRepository extends JpaRepository<AssetPurchaseInfo, Long> {
    
    List<AssetPurchaseInfo> findByAsset_AssetId(Long assetId);
    
    Optional<AssetPurchaseInfo> findByInvoiceNumberIgnoreCase(String invoiceNumber);
    
    Optional<AssetPurchaseInfo> findByBillNumberIgnoreCase(String billNumber);
    
    List<AssetPurchaseInfo> findByVendor_VendorId(Long vendorId);
    
    List<AssetPurchaseInfo> findByOutlet_OutletId(Long outletId);
    
    List<AssetPurchaseInfo> findByActiveTrueOrActiveIsNull();
}

