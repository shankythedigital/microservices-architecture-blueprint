package com.example.asset.dto;
import java.util.Set;

public class AssetDto {
    public String assetNameUdv;
    public Long categoryId;
    public Long subCategoryId;
    public Long makeId;
    public Long modelId;
    public String makeUdv;
    public String modelUdv;
    public String purchaseMode;
    public Long purchaseOutletId;
    public String purchaseOutletUdv;
    public String purchaseOutletAddressUdv;
    public String purchaseDate; // yyyy-MM-dd
    public String assetStatus;
    public String soldOnDate;
    public String salesChannelName;
    public Set<Long> componentIds;
    public String userId;     // to assign
    public String username;   // to assign
}
