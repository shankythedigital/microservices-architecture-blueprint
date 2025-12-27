
// // // // package com.example.asset.service;

// // // // public interface ValidationService {

// // // //     void validateCallerUser(String userId, String username);

// // // //     void validateUser(Long userId, String username);

// // // //     void validateAssetExists(Long assetId);

// // // //     void validateComponentExists(Long componentId);

// // // //     void validateAssetNotLinked(Long assetId, Long componentId);

// // // //     void validateAssetLinked(Long assetId, Long componentId, Long userId);
// // // // }


package com.example.asset.service;

import com.example.asset.dto.AssetUserUniversalLinkRequest;

public interface ValidationService {

    void validateLinkRequest(AssetUserUniversalLinkRequest request);

    boolean isAlreadyLinked(String entityType, Long entityId);

    void ensureEntityExists(String entityType, Long entityId);

    void ensureEntityLinked(String entityType, Long entityId, Long targetUserId);

    void validateLinkRequestSingle(String entityType, Long entityId, Long targetUserId, String targetUsername);

    void validateDelinkRequestSingle(String entityType, Long entityId, Long targetUserId);

    boolean isAlreadyLinkedToUser(String entityType, Long entityId, Long userId);

}


