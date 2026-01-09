package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationListResponse;
import com.example.notification.entity.*;
import com.example.notification.repository.*;
import com.example.notification.config.NotificationListProperties;
import com.example.notification.util.TemplateEngineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SmsLogRepository smsRepo;
    private final NotificationLogRepository notificationRepo;
    private final WhatsappLogRepository whatsappRepo;
    private final InappLogRepository inappRepo;
    private final TemplateResolverService templateResolver;
    private final NotificationListProperties listProperties;

    public NotificationService(
            SmsLogRepository smsRepo,
            NotificationLogRepository notificationRepo,
            WhatsappLogRepository whatsappRepo,
            InappLogRepository inappRepo,
            TemplateResolverService templateResolver,
            NotificationListProperties listProperties) {
        this.smsRepo = smsRepo;
        this.notificationRepo = notificationRepo;
        this.whatsappRepo = whatsappRepo;
        this.inappRepo = inappRepo;
        this.templateResolver = templateResolver;
        this.listProperties = listProperties;
    }

    @Transactional
    public void enqueue(NotificationRequest req) {
        // Step 1: Resolve template subject + body
        String rawBody = templateResolver.resolveBody(req.getChannel(), req.getTemplateCode());
        String subject = templateResolver.resolveSubject(req.getChannel(), req.getTemplateCode());

        // Step 2: Render body with placeholders
        String renderedBody = TemplateEngineUtil.render(rawBody, req.getPlaceholders());

        // Step 3: Persist based on channel
        switch (req.getChannel().toUpperCase()) {
            case "SMS" -> saveSms(req, renderedBody);
            case "WHATSAPP" -> saveWhatsapp(req, renderedBody);
            case "EMAIL", "NOTIFICATION" -> saveNotification(req, renderedBody, subject);
            case "INAPP" -> saveInapp(req, renderedBody);
            default -> throw new IllegalArgumentException("Unsupported channel: " + req.getChannel());
        }
    }

    private void saveSms(NotificationRequest req, String body) {
        SmsLog sms = new SmsLog();
        sms.setUsername(req.getUsername());
        sms.setMobile(req.getMobile());
        sms.setMessage(body);
        sms.setTemplateCode(req.getTemplateCode());
        sms.setUserId(req.getUserId());
        sms.setCreatedAt(LocalDateTime.now());

        // if (sms.getMobile() != null) {
        //     sms.setMobileFingerprint(HashUtil.fingerprint(sms.getMobile()));
        // }

        smsRepo.save(sms);
    }

    private void saveWhatsapp(NotificationRequest req, String body) {
        WhatsappLog wa = new WhatsappLog();
        wa.setUsername(req.getUsername());
        wa.setMobile(req.getMobile());
        wa.setMessage(body);
        wa.setTemplateCode(req.getTemplateCode());
        wa.setUserId(req.getUserId());
        wa.setCreatedAt(LocalDateTime.now());

        // if (wa.getMobile() != null) {
        //     wa.setMobileFingerprint(HashUtil.fingerprint(wa.getMobile()));
        // }

        whatsappRepo.save(wa);
    }

    private void saveNotification(NotificationRequest req, String body, String subject) {
        NotificationLog n = new NotificationLog();
        n.setUsername(req.getUsername());
        n.setEmail(req.getEmail());
        n.setSubject(subject != null ? subject : req.getSubject());
        n.setMessage(body);
        n.setChannel(req.getChannel());
        n.setTemplateCode(req.getTemplateCode());
        n.setUserId(req.getUserId());
        n.setCreatedAt(LocalDateTime.now());

        // if (n.getEmail() != null) {
        //     n.setEmailFingerprint(HashUtil.fingerprint(n.getEmail()));
        // }

        notificationRepo.save(n);
    }

    private void saveInapp(NotificationRequest req, String body) {
        InappLog in = new InappLog();
        in.setUsername(req.getUsername());
        in.setTitle(req.getSubject()); // in-app often uses a title
        in.setMessage(body);
        in.setTemplateCode(req.getTemplateCode());
        in.setUserId(req.getUserId());
        in.setCreatedAt(LocalDateTime.now());

        inappRepo.save(in);
    }

    // ============================================================
    // 📋 GET NOTIFICATION LIST FOR USER
    // ============================================================

    /**
     * Get notification list for a user filtered by configured days
     * Returns in-app notifications that should be displayed in notification icons
     * 
     * @param userId User ID to get notifications for
     * @param days Optional number of days (if null, uses configured displayDays)
     * @return List of notifications sorted by creation date (newest first)
     */
    @Transactional(readOnly = true)
    public List<NotificationListResponse> getNotificationList(String userId, Integer days) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Use provided days or default from configuration
        int displayDays = (days != null && days > 0) ? days : listProperties.getDisplayDays();
        
        // Calculate cutoff date
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(displayDays);
        
        log.info("📋 Fetching notifications for userId: {} within last {} days (since: {})", 
                userId, displayDays, cutoffDate);

        // Fetch notifications from database
        List<InappLog> notifications = inappRepo.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                userId, cutoffDate);

        // Limit results if configured
        int maxResults = listProperties.getMaxResults();
        if (notifications.size() > maxResults) {
            notifications = notifications.subList(0, maxResults);
            log.info("⚠️ Limited results to {} notifications (max configured: {})", 
                    maxResults, listProperties.getMaxResults());
        }

        // Map to response DTOs
        List<NotificationListResponse> response = notifications.stream()
                .map(this::mapToNotificationListResponse)
                .collect(Collectors.toList());

        log.info("✅ Retrieved {} notifications for userId: {}", response.size(), userId);
        return response;
    }

    /**
     * Get notification count for a user (unread count for notification badge)
     * 
     * @param userId User ID
     * @param days Optional number of days (if null, uses configured displayDays)
     * @return Count of notifications
     */
    @Transactional(readOnly = true)
    public Long getNotificationCount(String userId, Integer days) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        int displayDays = (days != null && days > 0) ? days : listProperties.getDisplayDays();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(displayDays);
        
        Long count = inappRepo.countByUserIdAndCreatedAtAfter(userId, cutoffDate);
        log.info("📊 Notification count for userId: {} (last {} days): {}", userId, displayDays, count);
        return count;
    }

    /**
     * Map InappLog entity to NotificationListResponse DTO
     */
    private NotificationListResponse mapToNotificationListResponse(InappLog inappLog) {
        NotificationListResponse response = new NotificationListResponse();
        response.setId(inappLog.getId());
        response.setUserId(inappLog.getUserId());
        response.setUsername(inappLog.getUsername());
        response.setTitle(inappLog.getTitle());
        response.setMessage(inappLog.getMessage());
        response.setTemplateCode(inappLog.getTemplateCode());
        response.setCreatedAt(inappLog.getCreatedAt());
        // Read status and priority can be added later when those fields are implemented
        response.setRead(false); // Default to unread
        response.setPriority(null); // Can be extracted from template or metadata later
        return response;
    }
}


