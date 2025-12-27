package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * ✅ AssetUserLinkClient
 * Fetches all users linked to assets under a given subcategory.
 */
@FeignClient(name = "asset-service", url = "${asset.service.url}")
public interface AssetUserLinkClient {

    @GetMapping("/api/asset/v1/userlinks/by-subcategory")
    List<Map<String, Object>> getUsersBySubCategory(@RequestParam("subCategoryId") Long subCategoryId);
}

