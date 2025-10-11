package com.example.asset.service;

import com.example.asset.dto.AssetDto;
import com.example.asset.entity.*;
import com.example.asset.repository.*;
import com.example.asset.service.client.NotificationClient;
import com.example.asset.dto.AssetNotificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AssetService {

    private final AssetMasterRepository assetRepo;
    private final ProductCategoryRepository catRepo;
    private final ProductSubCategoryRepository subRepo;
    private final ProductMakeRepository makeRepo;
    private final ProductModelRepository modelRepo;
    private final AssetComponentRepository compRepo;
    private final AssetUserLinkRepository linkRepo;
    private final NotificationClient notificationClient;

    public AssetService(AssetMasterRepository assetRepo,
                        ProductCategoryRepository catRepo,
                        ProductSubCategoryRepository subRepo,
                        ProductMakeRepository makeRepo,
                        ProductModelRepository modelRepo,
                        AssetComponentRepository compRepo,
                        AssetUserLinkRepository linkRepo,
                        NotificationClient notificationClient) {
        this.assetRepo = assetRepo;
        this.catRepo = catRepo;
        this.subRepo = subRepo;
        this.makeRepo = makeRepo;
        this.modelRepo = modelRepo;
        this.compRepo = compRepo;
        this.linkRepo = linkRepo;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public AssetMaster createAsset(AssetDto dto) throws Exception {
        AssetMaster a = new AssetMaster();
        a.setAssetNameUdv(dto.assetNameUdv);
        if (dto.categoryId != null) catRepo.findById(dto.categoryId).ifPresent(a::setCategory);
        if (dto.subCategoryId != null) subRepo.findById(dto.subCategoryId).ifPresent(a::setSubCategory);
        if (dto.makeId != null) makeRepo.findById(dto.makeId).ifPresent(a::setMake);
        if (dto.modelId != null) modelRepo.findById(dto.modelId).ifPresent(a::setModel);

        a.setMakeUdv(dto.makeUdv); a.setModelUdv(dto.modelUdv);
        a.setPurchaseMode(dto.purchaseMode);
        if (dto.purchaseDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            a.setPurchaseDate(sdf.parse(dto.purchaseDate));
        }
        a.setAssetStatus(Optional.ofNullable(dto.assetStatus).orElse("AVAILABLE"));
        if (dto.componentIds != null && !dto.componentIds.isEmpty()) {
            Set<AssetComponent> comps = new HashSet<>(compRepo.findAllById(dto.componentIds));
            a.setComponents(comps);
        }
        AssetMaster saved = assetRepo.save(a);

        // create user link if provided
        if (dto.userId != null) {
            AssetUserLink link = new AssetUserLink();
            link.setAsset(saved);
            link.setUserId(dto.userId);
            link.setUsername(dto.username);
            linkRepo.save(link);

            // fire notification
            try {
                AssetNotificationRequest req = new AssetNotificationRequest();
                req.channel = "EMAIL";
                req.username = dto.username;
                req.userId = dto.userId;
                req.templateCode = "ASSET_ASSIGNED";
                Map<String,Object> placeholders = new HashMap<>();
                placeholders.put("assetName", saved.getAssetNameUdv());
                placeholders.put("assetId", saved.getAssetId());
                req.placeholders = placeholders;
                notificationClient.sendNotification(req);
            } catch (Exception e) {
                // log and continue
                System.err.println("Notification failed: " + e.getMessage());
            }
        }

        return saved;
    }

    public Optional<AssetMaster> getAsset(Long id){ return assetRepo.findById(id); }
}
