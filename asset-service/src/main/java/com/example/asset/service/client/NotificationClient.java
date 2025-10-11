package com.example.asset.service.client;

import com.example.asset.dto.AssetNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notification-service", url = "${services.notification.base-url}", configuration = com.example.asset.config.FeignConfig.class)
public interface NotificationClient {
    @PostMapping("/api/notifications")
    void sendNotification(AssetNotificationRequest req);
}
