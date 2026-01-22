package com.example.asset.service;

import com.example.asset.dto.AssetScanCreateRequest;
import com.example.asset.dto.AssetScanResponse;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ✅ AssetScanService
 * Service for scanning QR codes and barcodes to identify assets.
 * Supports multiple matching strategies: Asset ID, Asset Name UDV, Serial Number.
 */
@Service
public class AssetScanService {

    private static final Logger log = LoggerFactory.getLogger(AssetScanService.class);

    private final AssetMasterRepository assetRepo;
    private final AuditAgentService auditService;
    private final AssetScanAiAgentService aiAgentService;
    private final AssetWarrantyRepository warrantyRepo;
    private final AssetAmcRepository amcRepo;
    private final AssetUserLinkRepository userLinkRepo;
    private final AssetComponentRepository componentRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ProductSubCategoryRepository subCategoryRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;

    public AssetScanService(AssetMasterRepository assetRepo, 
                           AuditAgentService auditService,
                           AssetScanAiAgentService aiAgentService,
                           AssetWarrantyRepository warrantyRepo,
                           AssetAmcRepository amcRepo,
                           AssetUserLinkRepository userLinkRepo,
                           AssetComponentRepository componentRepo,
                           ProductCategoryRepository categoryRepo,
                           ProductSubCategoryRepository subCategoryRepo,
                           ProductMakeRepository makeRepo,
                           ProductModelRepository modelRepo) {
        this.assetRepo = assetRepo;
        this.auditService = auditService;
        this.aiAgentService = aiAgentService;
        this.warrantyRepo = warrantyRepo;
        this.amcRepo = amcRepo;
        this.userLinkRepo = userLinkRepo;
        this.componentRepo = componentRepo;
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
    }

    // ============================================================
    // 📱 SCAN ASSET BY QR CODE / BARCODE
    // ============================================================
    @Transactional(readOnly = true)
    public Optional<AssetScanResponse> scanAsset(String scanValue, String scanType, 
                                                 String username, HttpServletRequest request) {
        if (!StringUtils.hasText(scanValue)) {
            log.warn("⚠️ Empty scan value provided");
            return Optional.empty();
        }

        log.info("📱 Scanning asset with value: '{}', type: '{}', user: '{}'", 
                scanValue, scanType, username);

        // Try different matching strategies
        Optional<AssetMaster> asset = Optional.empty();
        String matchedBy = null;

        // Strategy 1: Try as Asset ID (if numeric)
        if (isNumeric(scanValue)) {
            try {
                Long assetId = Long.parseLong(scanValue);
                asset = assetRepo.findById(assetId)
                        .filter(a -> a.getActive() == null || a.getActive());
                if (asset.isPresent()) {
                    matchedBy = "ASSET_ID";
                    log.debug("✅ Matched by Asset ID: {}", assetId);
                }
            } catch (NumberFormatException e) {
                log.debug("⚠️ Scan value is not a valid numeric ID: {}", scanValue);
            }
        }

        // Strategy 2: Try as Asset Name UDV (if not found by ID)
        if (asset.isEmpty()) {
            asset = assetRepo.findByAssetNameUdvIgnoreCase(scanValue.trim())
                    .filter(a -> a.getActive() == null || a.getActive());
            if (asset.isPresent()) {
                matchedBy = "ASSET_NAME_UDV";
                log.debug("✅ Matched by Asset Name UDV: {}", scanValue);
            }
        }

        // Strategy 3: Try as Serial Number (if not found by ID or Name)
        if (asset.isEmpty()) {
            asset = assetRepo.findBySerialNumberIgnoreCase(scanValue.trim())
                    .filter(a -> a.getActive() == null || a.getActive());
            if (asset.isPresent()) {
                matchedBy = "SERIAL_NUMBER";
                log.debug("✅ Matched by Serial Number: {}", scanValue);
            }
        }

        // Convert to response DTO
        if (asset.isPresent()) {
            AssetScanResponse response = convertToResponse(asset.get(), scanValue, scanType, matchedBy);
            
            // Log audit event
            String eventMessage = String.format("Asset scanned via %s: %s (Matched by: %s, Asset ID: %d)", 
                    scanType != null ? scanType : "AUTO", scanValue, matchedBy, asset.get().getAssetId());
            auditService.logEvent(username != null ? username : "SYSTEM", eventMessage, request);
            
            log.info("✅ Asset scan successful: Asset ID={}, Matched by={}", 
                    asset.get().getAssetId(), matchedBy);
            return Optional.of(response);
        } else {
            // Log failed scan attempt
            String eventMessage = String.format("Asset scan failed: No asset found for scan value '%s' (type: %s)", 
                    scanValue, scanType != null ? scanType : "AUTO");
            auditService.logEvent(username != null ? username : "SYSTEM", eventMessage, request);
            
            log.warn("⚠️ Asset scan failed: No asset found for value '{}'", scanValue);
            return Optional.empty();
        }
    }

    // ============================================================
    // 🔄 CONVERT ENTITY TO RESPONSE DTO
    // ============================================================
    private AssetScanResponse convertToResponse(AssetMaster asset, String scanValue, 
                                                String scanType, String matchedBy) {
        AssetScanResponse response = new AssetScanResponse();
        
        response.setAssetId(asset.getAssetId());
        response.setAssetNameUdv(asset.getAssetNameUdv());
        response.setSerialNumber(asset.getSerialNumber());
        response.setAssetStatus(asset.getAssetStatus());
        response.setPurchaseDate(asset.getPurchaseDate());
        
        // Master Data
        if (asset.getCategory() != null) {
            response.setCategoryName(asset.getCategory().getCategoryName());
        }
        if (asset.getSubCategory() != null) {
            response.setSubCategoryName(asset.getSubCategory().getSubCategoryName());
        }
        if (asset.getMake() != null) {
            response.setMakeName(asset.getMake().getMakeName());
        }
        if (asset.getModel() != null) {
            response.setModelName(asset.getModel().getModelName());
        }
        
        // Scan Metadata
        response.setMatchedBy(matchedBy);
        response.setScanValue(scanValue);
        response.setScanType(scanType != null ? scanType : "AUTO");
        
        return response;
    }

    // ============================================================
    // 📱 SCAN AND CREATE/UPDATE ASSET (with AI Agent)
    // ============================================================
    @Transactional
    public AssetScanResponse scanAndSave(HttpHeaders headers, AssetScanCreateRequest request, 
                                         HttpServletRequest httpRequest) {
        log.info("📱 [SCAN & SAVE] Processing scan value: '{}'", request.getScanValue());
        
        String username = request.getUsername() != null ? request.getUsername() : "SYSTEM";
        Long userId = request.getUserId();
        
        // Step 1: Use AI Agent to analyze and extract data
        AssetScanCreateRequest enrichedRequest = aiAgentService.analyzeAndExtract(
                request.getScanValue(), 
                request.getScanType(),
                userId,
                username
        );
        
        // Merge explicit request data with AI-extracted data
        mergeRequestData(request, enrichedRequest);
        
        // Step 2: Find or create asset
        AssetMaster asset = findOrCreateAsset(enrichedRequest, username);
        
        // Step 3: Create/update warranty if data exists
        if (enrichedRequest.getWarranty() != null) {
            createOrUpdateWarranty(asset, enrichedRequest.getWarranty(), username, userId);
        }
        
        // Step 4: Create/update AMC if data exists
        if (enrichedRequest.getAmc() != null) {
            createOrUpdateAmc(asset, enrichedRequest.getAmc(), username, userId);
        }
        
        // Step 5: Link user if specified
        if (enrichedRequest.getTargetUserId() != null) {
            linkUserToAsset(asset, enrichedRequest.getTargetUserId(), 
                          enrichedRequest.getTargetUsername(), username);
        }
        
        // Step 6: Link components if specified
        if (enrichedRequest.getComponentIds() != null && !enrichedRequest.getComponentIds().isEmpty()) {
            linkComponentsToAsset(asset, enrichedRequest.getComponentIds());
        }
        
        // Step 7: Convert to response and log audit
        AssetScanResponse response = convertToResponse(asset, request.getScanValue(), 
                                                       request.getScanType(), "CREATED_OR_UPDATED");
        
        String eventMessage = String.format("Asset scanned and saved via %s: %s (Asset ID: %d)", 
                request.getScanType() != null ? request.getScanType() : "AUTO", 
                request.getScanValue(), asset.getAssetId());
        auditService.logEvent(username, eventMessage, httpRequest);
        
        log.info("✅ Asset scan and save successful: Asset ID={}", asset.getAssetId());
        return response;
    }
    
    // ============================================================
    // 🔍 FIND OR CREATE ASSET
    // ============================================================
    private AssetMaster findOrCreateAsset(AssetScanCreateRequest request, String username) {
        // Try to find existing asset
        Optional<AssetMaster> existing = Optional.empty();
        
        if (StringUtils.hasText(request.getSerialNumber())) {
            existing = assetRepo.findBySerialNumberIgnoreCase(request.getSerialNumber().trim())
                    .filter(a -> a.getActive() == null || a.getActive());
        }
        
        if (existing.isEmpty() && StringUtils.hasText(request.getAssetNameUdv())) {
            existing = assetRepo.findByAssetNameUdvIgnoreCase(request.getAssetNameUdv().trim())
                    .filter(a -> a.getActive() == null || a.getActive());
        }
        
        if (existing.isPresent()) {
            // Update existing asset
            AssetMaster asset = existing.get();
            if (StringUtils.hasText(request.getAssetNameUdv()) && 
                !request.getAssetNameUdv().equals(asset.getAssetNameUdv())) {
                asset.setAssetNameUdv(request.getAssetNameUdv());
            }
            if (StringUtils.hasText(request.getSerialNumber()) && 
                !request.getSerialNumber().equals(asset.getSerialNumber())) {
                asset.setSerialNumber(request.getSerialNumber());
            }
            if (request.getPurchaseDate() != null) {
                asset.setPurchaseDate(request.getPurchaseDate());
            }
            if (StringUtils.hasText(request.getAssetStatus())) {
                asset.setAssetStatus(request.getAssetStatus());
            }
            
            // Update relationships if provided
            if (request.getCategoryId() != null) {
                categoryRepo.findById(request.getCategoryId())
                        .ifPresent(asset::setCategory);
            }
            if (request.getSubCategoryId() != null) {
                subCategoryRepo.findById(request.getSubCategoryId())
                        .ifPresent(asset::setSubCategory);
            }
            if (request.getMakeId() != null) {
                makeRepo.findById(request.getMakeId())
                        .ifPresent(asset::setMake);
            }
            if (request.getModelId() != null) {
                modelRepo.findById(request.getModelId())
                        .ifPresent(asset::setModel);
            }
            
            asset.setUpdatedBy(username);
            return assetRepo.save(asset);
        } else {
            // Create new asset
            if (!StringUtils.hasText(request.getAssetNameUdv())) {
                throw new IllegalArgumentException("❌ Asset name is required for creation");
            }
            
            if (assetRepo.existsByAssetNameUdv(request.getAssetNameUdv())) {
                throw new IllegalArgumentException("❌ Asset with name '" + request.getAssetNameUdv() + "' already exists");
            }
            
            AssetMaster asset = new AssetMaster();
            asset.setAssetNameUdv(request.getAssetNameUdv());
            asset.setSerialNumber(request.getSerialNumber());
            asset.setPurchaseDate(request.getPurchaseDate());
            asset.setAssetStatus(StringUtils.hasText(request.getAssetStatus()) ? 
                    request.getAssetStatus() : "ACTIVE");
            asset.setCreatedBy(username);
            asset.setUpdatedBy(username);
            asset.setActive(true);
            
            // Set relationships
            if (request.getCategoryId() != null) {
                categoryRepo.findById(request.getCategoryId())
                        .ifPresent(asset::setCategory);
            }
            if (request.getSubCategoryId() != null) {
                subCategoryRepo.findById(request.getSubCategoryId())
                        .ifPresent(asset::setSubCategory);
            }
            if (request.getMakeId() != null) {
                makeRepo.findById(request.getMakeId())
                        .ifPresent(asset::setMake);
            }
            if (request.getModelId() != null) {
                modelRepo.findById(request.getModelId())
                        .ifPresent(asset::setModel);
            }
            
            return assetRepo.save(asset);
        }
    }
    
    // ============================================================
    // 🛡️ CREATE OR UPDATE WARRANTY
    // ============================================================
    private void createOrUpdateWarranty(AssetMaster asset, AssetScanCreateRequest.WarrantyData warrantyData, 
                                      String username, Long userId) {
        Optional<AssetWarranty> existing = warrantyRepo.findByAsset_AssetId(asset.getAssetId())
                .stream()
                .filter(w -> w.getActive() == null || w.getActive())
                .findFirst();
        
        AssetWarranty warranty;
        if (existing.isPresent()) {
            warranty = existing.get();
        } else {
            warranty = new AssetWarranty();
            warranty.setAsset(asset);
            warranty.setCreatedBy(username);
            warranty.setActive(true);
        }
        
        if (StringUtils.hasText(warrantyData.getWarrantyStatus())) {
            warranty.setWarrantyStatus(warrantyData.getWarrantyStatus());
        }
        if (StringUtils.hasText(warrantyData.getWarrantyProvider())) {
            warranty.setWarrantyProvider(warrantyData.getWarrantyProvider());
        }
        if (StringUtils.hasText(warrantyData.getWarrantyTerms())) {
            warranty.setWarrantyTerms(warrantyData.getWarrantyTerms());
        }
        if (warrantyData.getStartDate() != null) {
            warranty.setWarrantyStartDate(warrantyData.getStartDate());
        }
        if (warrantyData.getEndDate() != null) {
            warranty.setWarrantyEndDate(warrantyData.getEndDate());
        }
        if (warrantyData.getComponentId() != null) {
            warranty.setComponentId(warrantyData.getComponentId());
        }
        if (warrantyData.getDocumentId() != null) {
            warranty.setDocumentId(warrantyData.getDocumentId());
        }
        
        warranty.setUserId(userId);
        warranty.setUsername(username);
        warranty.setUpdatedBy(username);
        
        warrantyRepo.save(warranty);
        log.debug("✅ Warranty created/updated for asset ID: {}", asset.getAssetId());
    }
    
    // ============================================================
    // 📋 CREATE OR UPDATE AMC
    // ============================================================
    private void createOrUpdateAmc(AssetMaster asset, AssetScanCreateRequest.AmcData amcData, 
                                  String username, Long userId) {
        Optional<AssetAmc> existing = amcRepo.findByAsset_AssetId(asset.getAssetId())
                .stream()
                .filter(a -> a.getActive() == null || a.getActive())
                .findFirst();
        
        AssetAmc amc;
        if (existing.isPresent()) {
            amc = existing.get();
        } else {
            amc = new AssetAmc();
            amc.setAsset(asset);
            amc.setCreatedBy(username);
            amc.setActive(true);
        }
        
        if (StringUtils.hasText(amcData.getAmcStatus())) {
            amc.setAmcStatus(amcData.getAmcStatus());
        }
        if (amcData.getStartDate() != null) {
            amc.setStartDate(amcData.getStartDate());
        }
        if (amcData.getEndDate() != null) {
            amc.setEndDate(amcData.getEndDate());
        }
        if (amcData.getComponentId() != null) {
            amc.setComponentId(amcData.getComponentId());
        }
        if (amcData.getDocumentId() != null) {
            amc.setDocumentId(amcData.getDocumentId());
        }
        
        amc.setUserId(userId);
        amc.setUsername(username);
        amc.setUpdatedBy(username);
        
        amcRepo.save(amc);
        log.debug("✅ AMC created/updated for asset ID: {}", asset.getAssetId());
    }
    
    // ============================================================
    // 👥 LINK USER TO ASSET
    // ============================================================
    private void linkUserToAsset(AssetMaster asset, Long targetUserId, String targetUsername, String createdBy) {
        // Check if already linked
        if (userLinkRepo.existsByAssetIdAndUserIdAndActiveTrue(asset.getAssetId(), targetUserId)) {
            log.debug("⚠️ User {} already linked to asset {}", targetUserId, asset.getAssetId());
            return;
        }
        
        AssetUserLink link = new AssetUserLink();
        link.setAssetId(asset.getAssetId());
        link.setUserId(targetUserId);
        link.setUsername(targetUsername != null ? targetUsername : String.valueOf(targetUserId));
        link.setAssignedDate(LocalDateTime.now());
        link.setCreatedBy(createdBy);
        link.setUpdatedBy(createdBy);
        link.setActive(true);
        
        userLinkRepo.save(link);
        log.debug("✅ User {} linked to asset {}", targetUserId, asset.getAssetId());
    }
    
    // ============================================================
    // 🔧 LINK COMPONENTS TO ASSET
    // ============================================================
    private void linkComponentsToAsset(AssetMaster asset, List<Long> componentIds) {
        Set<AssetComponent> components = new HashSet<>(asset.getComponents());
        
        for (Long componentId : componentIds) {
            componentRepo.findById(componentId).ifPresent(components::add);
        }
        
        asset.setComponents(components);
        assetRepo.save(asset);
        log.debug("✅ Linked {} components to asset {}", componentIds.size(), asset.getAssetId());
    }
    
    // ============================================================
    // 🔄 MERGE REQUEST DATA
    // ============================================================
    private void mergeRequestData(AssetScanCreateRequest source, AssetScanCreateRequest target) {
        // Merge explicit values (explicit takes precedence)
        if (StringUtils.hasText(source.getAssetNameUdv())) {
            target.setAssetNameUdv(source.getAssetNameUdv());
        }
        if (StringUtils.hasText(source.getSerialNumber())) {
            target.setSerialNumber(source.getSerialNumber());
        }
        if (source.getCategoryId() != null) {
            target.setCategoryId(source.getCategoryId());
        }
        if (source.getSubCategoryId() != null) {
            target.setSubCategoryId(source.getSubCategoryId());
        }
        if (source.getMakeId() != null) {
            target.setMakeId(source.getMakeId());
        }
        if (source.getModelId() != null) {
            target.setModelId(source.getModelId());
        }
        if (source.getWarranty() != null) {
            target.setWarranty(source.getWarranty());
        }
        if (source.getAmc() != null) {
            target.setAmc(source.getAmc());
        }
        if (source.getTargetUserId() != null) {
            target.setTargetUserId(source.getTargetUserId());
        }
        if (source.getComponentIds() != null) {
            target.setComponentIds(source.getComponentIds());
        }
    }
    
    // ============================================================
    // 🧩 HELPER METHODS
    // ============================================================
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

