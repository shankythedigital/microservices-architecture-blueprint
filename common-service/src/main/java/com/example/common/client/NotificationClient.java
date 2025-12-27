
package com.example.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;


@FeignClient(name = "notification-service", url = "${notification.service.url}")

public interface NotificationClient {

    @PostMapping
    // void sendNotification (@RequestBody Map<String, Object> payload); 
    void sendNotification (@RequestBody Map<String, Object> payload,
    @RequestHeader("Authorization") String bearerToken); 

    // void sendNotification(
    // @RequestParam("mobile") String mobile, 
    // @RequestParam("username") String username,
    // @RequestParam("email") String email,
    // @RequestParam("templateCode") String templateCode,
    // @RequestParam Map<String, String> placeholders);

    
}

