
package com.example.common.service;

import com.example.common.client.NotificationClient;
import com.example.common.util.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ✅ Shared notification utility for all microservices.
 * 
 * - Centralizes logic for building and sending notifications.
 * - Uses AuthTokenService for secure inter-service authentication.
 * - Safe to include in any service; no cyclic dependencies on auth-service.
 */
@Component
@ConditionalOnProperty(name = "common.notification.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationHelper {

    private static final Logger log = LoggerFactory.getLogger(NotificationHelper.class);

    private final NotificationClient notificationClient;
    private final AuthTokenService authTokenService;

    public NotificationHelper(@Nullable NotificationClient notificationClient,
                              @Nullable AuthTokenService authTokenService) {
        this.notificationClient = notificationClient;
        this.authTokenService = authTokenService;
    }

    /**
     * Send a notification using the Notification Service.
     */
    public void sendNotification(String bearerToken,
                                 Long userId,
                                 String username,
                                 String email,
                                 String mobile,
                                 String channel,
                                 String templateCode,
                                 Map<String, Object> placeholders,
                                 Map<String, Object> audit,
                                 String projectType) {
        if (notificationClient == null) {
            log.warn("⚠️ NotificationClient not available — skipping notification ({} for {})",
                     templateCode, username);
            return;
        }

        // String bearerToken = resolveToken();

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("userId", userId);
        req.put("username", username);
        req.put("mobile", mobile);
        req.put("email", email);
        if (channel == null) {
            channel = "INAPP";
        }
        
        req.put("channel", channel.toUpperCase());
        
        if (!templateCode.contains("_" + channel.toUpperCase())) {
            templateCode = templateCode + "_" + channel.toUpperCase();
        }
        
        req.put("templateCode", templateCode);
        req.put("placeholders", placeholders == null ? Map.of() : placeholders);
        req.put("audit", audit == null ? buildAudit() : audit);
        req.put("projectType", projectType);

        try {
            notificationClient.sendNotification(req, bearerToken);
            log.info("📤 Sent notification via {} for user {}", channel, username);
        } catch (Exception e) {
            log.error("❌ Failed to send notification via {} for user {}: {}", channel, username, e.getMessage(), e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }

    /**
     * Async variant for non-blocking sends.
     */
    @Async
    public void sendNotificationAsync(String bearertoken,
                                      Long userId,
                                      String username,
                                      String email,
                                      String mobile,
                                      String channel,
                                      String templateCode,
                                      Map<String, Object> placeholders,
                                      Map<String, Object> audit,
                                      String projectType) {
        sendNotification(bearertoken,userId, username, email, mobile, channel, templateCode, placeholders, audit, projectType);
    }

    /**
     * Builds standard request audit metadata.
     */
    public Map<String, Object> buildAudit() {
        Map<String, Object> audit = new LinkedHashMap<>();
        try {
            audit.put("ipAddress", RequestContext.getIp());
            audit.put("userAgent", RequestContext.getUserAgent());
            audit.put("url", RequestContext.getUrl());
            audit.put("httpMethod", RequestContext.getMethod());
            audit.put("sessionId", RequestContext.getSessionId());
        } catch (Exception e) {
            log.debug("Audit context not available: {}", e.getMessage());
        }
        return audit;
    }

    /**
     * Resolves a Bearer token via AuthTokenService.
     * Falls back to null if no token can be obtained.
     */
    private String resolveToken() {
        try {
            String token = authTokenService != null ? authTokenService.getAccessToken() : null;
            if (token != null && !token.isBlank()) {
                return token.startsWith("Bearer ") ? token : "Bearer " + token;
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not resolve service token: {}", e.getMessage());
        }
        return null;
    }
}

