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
import java.util.Map;
import java.util.Optional;
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
     * @return Count of unread notifications
     */
    @Transactional(readOnly = true)
    public Long getNotificationCount(String userId, Integer days) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        int displayDays = (days != null && days > 0) ? days : listProperties.getDisplayDays();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(displayDays);
        
        // Count unread notifications (isRead = false or null)
        List<InappLog> notifications = inappRepo.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, cutoffDate);
        long count = notifications.stream()
                .filter(n -> n.getIsRead() == null || !n.getIsRead())
                .count();
        
        log.info("📊 Unread notification count for userId: {} (last {} days): {}", userId, displayDays, count);
        return count;
    }

    // ============================================================
    // ✅ MARK NOTIFICATIONS AS READ
    // ============================================================

    /**
     * Mark a single notification as read
     * 
     * @param notificationId Notification ID
     * @param userId User ID (for security - ensures user can only mark their own notifications)
     * @return true if notification was marked as read, false if not found or already read
     */
    @Transactional
    public boolean markNotificationAsRead(Long notificationId, String userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Find notification and verify it belongs to the user
        Optional<InappLog> notificationOpt = inappRepo.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isEmpty()) {
            log.warn("⚠️ Notification {} not found or does not belong to user {}", notificationId, userId);
            return false;
        }

        InappLog notification = notificationOpt.get();

        // Check if already read
        if (Boolean.TRUE.equals(notification.getIsRead())) {
            log.info("ℹ️ Notification {} is already marked as read", notificationId);
            return false;
        }

        // Mark as read
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        inappRepo.save(notification);
        
        log.info("✅ Marked notification {} as read for userId: {}", notificationId, userId);
        return true;
    }

    /**
     * Mark all notifications as read for a user
     * 
     * @param userId User ID
     * @param days Optional number of days (if null, uses configured displayDays)
     * @return Number of notifications marked as read
     */
    @Transactional
    public int markAllNotificationsAsRead(String userId, Integer days) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        int displayDays = (days != null && days > 0) ? days : listProperties.getDisplayDays();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(displayDays);
        LocalDateTime readAt = LocalDateTime.now();

        // Find all unread notifications for the user within the date range
        List<InappLog> notifications = inappRepo.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, cutoffDate);
        
        // Filter unread notifications and mark them as read
        int updated = 0;
        for (InappLog notification : notifications) {
            if (notification.getIsRead() == null || !notification.getIsRead()) {
                notification.setIsRead(true);
                notification.setReadAt(readAt);
                inappRepo.save(notification);
                updated++;
            }
        }
        
        log.info("✅ Marked {} notifications as read for userId: {} (last {} days)", updated, userId, displayDays);
        return updated;
    }

    /**
     * Mark a single notification as unread
     * 
     * @param notificationId Notification ID
     * @param userId User ID (for security - ensures user can only mark their own notifications)
     * @return true if notification was marked as unread, false if not found or already unread
     */
    @Transactional
    public boolean markNotificationAsUnread(Long notificationId, String userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Find notification and verify it belongs to the user
        Optional<InappLog> notificationOpt = inappRepo.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isEmpty()) {
            log.warn("⚠️ Notification {} not found or does not belong to user {}", notificationId, userId);
            return false;
        }

        InappLog notification = notificationOpt.get();

        // Check if already unread
        if (notification.getIsRead() == null || !notification.getIsRead()) {
            log.info("ℹ️ Notification {} is already marked as unread", notificationId);
            return false;
        }

        // Mark as unread
        notification.setIsRead(false);
        notification.setReadAt(null);
        inappRepo.save(notification);
        
        log.info("✅ Marked notification {} as unread for userId: {}", notificationId, userId);
        return true;
    }

    /**
     * Mark all notifications as unread for a user
     * 
     * @param userId User ID
     * @param days Optional number of days (if null, uses configured displayDays)
     * @return Number of notifications marked as unread
     */
    @Transactional
    public int markAllNotificationsAsUnread(String userId, Integer days) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        int displayDays = (days != null && days > 0) ? days : listProperties.getDisplayDays();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(displayDays);

        // Find all read notifications for the user within the date range
        List<InappLog> notifications = inappRepo.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, cutoffDate);
        
        // Filter read notifications and mark them as unread
        int updated = 0;
        for (InappLog notification : notifications) {
            if (Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(false);
                notification.setReadAt(null);
                inappRepo.save(notification);
                updated++;
            }
        }
        
        log.info("✅ Marked {} notifications as unread for userId: {} (last {} days)", updated, userId, displayDays);
        return updated;
    }

    /**
     * Toggle read status of a notification (unread -> read, read -> unread)
     * 
     * @param notificationId Notification ID
     * @param userId User ID (for security - ensures user can only toggle their own notifications)
     * @return Map containing the notification ID, new read status, and success flag
     */
    @Transactional
    public Map<String, Object> toggleNotificationReadStatus(Long notificationId, String userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Find notification and verify it belongs to the user
        Optional<InappLog> notificationOpt = inappRepo.findByIdAndUserId(notificationId, userId);
        if (notificationOpt.isEmpty()) {
            log.warn("⚠️ Notification {} not found or does not belong to user {}", notificationId, userId);
            throw new IllegalArgumentException("Notification not found or does not belong to the user");
        }

        InappLog notification = notificationOpt.get();
        
        // Toggle read status
        boolean currentStatus = Boolean.TRUE.equals(notification.getIsRead());
        boolean newStatus = !currentStatus;
        
        notification.setIsRead(newStatus);
        if (newStatus) {
            // If marking as read, set read timestamp
            notification.setReadAt(LocalDateTime.now());
        } else {
            // If marking as unread, clear read timestamp
            notification.setReadAt(null);
        }
        
        inappRepo.save(notification);
        
        log.info("✅ Toggled notification {} read status from {} to {} for userId: {}", 
                notificationId, currentStatus, newStatus, userId);
        
        return Map.of(
                "notificationId", notificationId,
                "userId", userId,
                "previousStatus", currentStatus ? "read" : "unread",
                "newStatus", newStatus ? "read" : "unread",
                "isRead", newStatus,
                "readAt", newStatus ? notification.getReadAt() : null
        );
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
        response.setRead(Boolean.TRUE.equals(inappLog.getIsRead())); // Use actual read status from entity
        response.setPriority(null); // Can be extracted from template or metadata later
        return response;
    }
}


