
package com.example.common.service;

import com.example.common.client.NotificationClient;
import com.example.common.entity.NotificationRetryLog;
import com.example.common.repository.NotificationRetryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ✅ SafeNotificationHelper (v2)
 *
 * - Retries transient notification failures
 * - Persists failed notifications to DB for retry
 * - Optionally publishes to Kafka (future extension)
 */
@Component
@ConditionalOnProperty(name = "common.notification.enabled", havingValue = "true", matchIfMissing = true)
public class SafeNotificationHelper {

    private static final int MAX_RETRIES = 3;

    @Autowired
    private NotificationHelper notificationHelper;

    @Autowired
    private NotificationRetryLogRepository retryLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private NotificationClient notificationClient; // Optional, used internally

    /**
     * ✅ Safe send with retry and persistence fallback
     */
    public void safeNotify(String bearertoken,
                           Long userId,
                           String username,
                           String email,
                           String mobile,
                           String channel,
                           String templateCode,
                           Map<String, Object> placeholders,
                           String projectType) {

        Map<String, Object> audit = notificationHelper.buildAudit();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                notificationHelper.sendNotification(bearertoken,
                        userId, username, email, mobile,
                        channel, templateCode, placeholders, audit, projectType
                );
                System.out.printf("📨 Notification via %s succeeded on attempt %d for %s%n",
                        channel, attempt, username);
                return;
            } catch (Exception ex) {
                System.err.printf("⚠️ Attempt %d/%d failed via %s for %s: %s%n",
                        attempt, MAX_RETRIES, channel, username, ex.getMessage());

                if (attempt == MAX_RETRIES) {
                    persistFailure(userId, username, channel, templateCode, placeholders, ex, attempt);
                }

                sleep(500);
            }
        }
    }

    /**
     * 🧵 Async variant (non-blocking)
     */
    @Async
    public void safeNotifyAsync(String bearertoken,
                                Long userId,
                                String username,
                                String email,
                                String mobile,
                                String channel,
                                String templateCode,
                                Map<String, Object> placeholders,
                                String projectType) {
        safeNotify(bearertoken,userId, username, email, mobile, channel, templateCode, placeholders, projectType);
    }

    /**
     * 🧠 Persist failed attempt for later retry (DB or Kafka)
     */
    private void persistFailure(Long userId,
                                String username,
                                String channel,
                                String templateCode,
                                Map<String, Object> payload,
                                Exception ex,
                                int retryCount) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            NotificationRetryLog log = new NotificationRetryLog(
                    userId, username, channel, templateCode, payloadJson, ex.getMessage(), retryCount
            );
            retryLogRepository.save(log);
            System.err.printf("💾 Stored failed notification for retry (%s, %s)%n", username, channel);
        } catch (Exception e) {
            System.err.printf("❌ Could not persist failed notification: %s%n", e.getMessage());
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}

