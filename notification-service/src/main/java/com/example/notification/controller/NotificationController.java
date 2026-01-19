package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationListResponse;
import com.example.notification.service.NotificationService;
import com.example.common.security.JwtVerifier;
import com.example.common.util.ResponseWrapper;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService service;
    private final JwtVerifier jwtVerifier;

    public NotificationController(NotificationService service, JwtVerifier jwtVerifier) {
        this.service = service;
        this.jwtVerifier = jwtVerifier;
    }

    @PostMapping
    public ResponseEntity<String> send(@Valid @RequestBody NotificationRequest req) {
        service.enqueue(req);
        
        return ResponseEntity.accepted().body(req.getChannel() + " Notification accepted");
    }

    // ============================================================
    // 📋 GET NOTIFICATION LIST
    // ============================================================

    /**
     * Get notification list for the logged-in user
     * GET /api/notifications/list - Get current user's notifications
     * Returns in-app notifications filtered by configured days (default: 30 days)
     */
    @GetMapping("/list")
    @Operation(summary = "Get notification list for logged-in user",
               description = "Returns in-app notifications for the logged-in user filtered by configured days. " +
                           "Notifications are sorted by creation date (newest first). " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30). " +
                           "Optionally, you can specify a userId query parameter to get notifications for a specific user.")
    public ResponseEntity<ResponseWrapper<List<NotificationListResponse>>> getNotificationList(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            List<NotificationListResponse> notifications = service.getNotificationList(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Notifications retrieved successfully for userId: " + userId,
                    notifications
            ));
        } catch (Exception e) {
            log.error("❌ Failed to retrieve notification list: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get notification list by userId (path parameter)
     * GET /api/notifications/list/{userId} - Get notifications for a specific user
     * Returns in-app notifications filtered by configured days (default: 30 days)
     */
    @GetMapping("/list/{userId}")
    @Operation(summary = "Get notification list by userId",
               description = "Returns in-app notifications for a specific user ID filtered by configured days. " +
                           "Notifications are sorted by creation date (newest first). " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30).")
    public ResponseEntity<ResponseWrapper<List<NotificationListResponse>>> getNotificationListByUserId(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @RequestParam(value = "days", required = false) Integer days) {
        
        try {
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "User ID is required", null));
            }

            List<NotificationListResponse> notifications = service.getNotificationList(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Notifications retrieved successfully for userId: " + userId,
                    notifications
            ));
        } catch (Exception e) {
            log.error("❌ Failed to retrieve notification list for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get notification count for the logged-in user (for badge display)
     * GET /api/notifications/count - Get current user's notification count
     */
    @GetMapping("/count")
    @Operation(summary = "Get notification count for logged-in user",
               description = "Returns the count of in-app notifications for the logged-in user within configured days. " +
                           "Useful for displaying notification badge count. " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30). " +
                           "Optionally, you can specify a userId query parameter to get count for a specific user.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getNotificationCount(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            Long count = service.getNotificationCount(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Notification count retrieved successfully",
                    Map.of("userId", userId, "count", count, "days", days != null ? days : 30)
            ));
        } catch (Exception e) {
            log.error("❌ Failed to retrieve notification count: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get notification count by userId (path parameter)
     * GET /api/notifications/count/{userId} - Get notification count for a specific user
     */
    @GetMapping("/count/{userId}")
    @Operation(summary = "Get notification count by userId",
               description = "Returns the count of in-app notifications for a specific user ID within configured days. " +
                           "Useful for displaying notification badge count. " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30).")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getNotificationCountByUserId(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @RequestParam(value = "days", required = false) Integer days) {
        
        try {
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "User ID is required", null));
            }

            Long count = service.getNotificationCount(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Notification count retrieved successfully",
                    Map.of("userId", userId, "count", count, "days", days != null ? days : 30)
            ));
        } catch (Exception e) {
            log.error("❌ Failed to retrieve notification count for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ✅ MARK NOTIFICATIONS AS READ
    // ============================================================

    /**
     * Mark a single notification as read
     * PUT /api/notifications/read/{notificationId} - Mark a specific notification as read
     */
    @PutMapping("/read/{notificationId}")
    @Operation(summary = "Mark a single notification as read",
               description = "Marks a specific notification as read for the logged-in user. " +
                           "The notification must belong to the logged-in user. " +
                           "Optionally, you can specify a userId query parameter to mark notification for a specific user.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> markNotificationAsRead(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long notificationId,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            boolean marked = service.markNotificationAsRead(notificationId, userId);
            
            if (marked) {
                return ResponseEntity.ok(new ResponseWrapper<>(
                        true,
                        "Notification marked as read successfully",
                        Map.of("notificationId", notificationId, "userId", userId, "read", true)
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(
                                false,
                                "Notification not found, already read, or does not belong to the user",
                                Map.of("notificationId", notificationId, "userId", userId, "read", false)
                        ));
            }
        } catch (Exception e) {
            log.error("❌ Failed to mark notification {} as read: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Mark all notifications as read for the logged-in user
     * PUT /api/notifications/read-all - Mark all notifications as read for current user
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for logged-in user",
               description = "Marks all unread notifications as read for the logged-in user within configured days. " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30). " +
                           "Optionally, you can specify userId and days query parameters.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> markAllNotificationsAsRead(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            int markedCount = service.markAllNotificationsAsRead(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "All notifications marked as read successfully",
                    Map.of("userId", userId, "markedCount", markedCount, "days", days != null ? days : 30)
            ));
        } catch (Exception e) {
            log.error("❌ Failed to mark all notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Mark all notifications as read by userId (path parameter)
     * PUT /api/notifications/read-all/{userId} - Mark all notifications as read for a specific user
     */
    @PutMapping("/read-all/{userId}")
    @Operation(summary = "Mark all notifications as read by userId",
               description = "Marks all unread notifications as read for a specific user ID within configured days. " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30).")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> markAllNotificationsAsReadByUserId(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @RequestParam(value = "days", required = false) Integer days) {
        
        try {
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "User ID is required", null));
            }

            int markedCount = service.markAllNotificationsAsRead(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "All notifications marked as read successfully",
                    Map.of("userId", userId, "markedCount", markedCount, "days", days != null ? days : 30)
            ));
        } catch (Exception e) {
            log.error("❌ Failed to mark all notifications as read for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Toggle read status of a notification (unread -> read, read -> unread)
     * PATCH /api/notifications/toggle-read/{notificationId} - Toggle read status of a notification
     */
    @PatchMapping("/toggle-read/{notificationId}")
    @Operation(summary = "Toggle read status of a notification",
               description = "Toggles the read status of a specific notification. " +
                           "If the notification is unread, it will be marked as read. " +
                           "If it's already read, it will be marked as unread. " +
                           "The notification must belong to the logged-in user. " +
                           "Optionally, you can specify a userId query parameter.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> toggleNotificationReadStatus(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long notificationId,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            Map<String, Object> result = service.toggleNotificationReadStatus(notificationId, userId);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Notification read status toggled successfully from " + result.get("previousStatus") + " to " + result.get("newStatus"),
                    result
            ));
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Failed to toggle notification {} read status: {}", notificationId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to toggle notification {} read status: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Toggle read status by userId (path parameter)
     * PATCH /api/notifications/toggle-read/{notificationId}/{userId} - Toggle read status for a specific user
     */
    @PatchMapping("/toggle-read/{notificationId}/{userId}")
    @Operation(summary = "Toggle read status by userId",
               description = "Toggles the read status of a specific notification for a given user ID. " +
                           "If the notification is unread, it will be marked as read. " +
                           "If it's already read, it will be marked as unread.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> toggleNotificationReadStatusByUserId(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long notificationId,
            @PathVariable String userId) {
        
        try {
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "User ID is required", null));
            }

            Map<String, Object> result = service.toggleNotificationReadStatus(notificationId, userId);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "Notification read status toggled successfully from " + result.get("previousStatus") + " to " + result.get("newStatus"),
                    result
            ));
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Failed to toggle notification {} read status for userId {}: {}", notificationId, userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Failed to toggle notification {} read status for userId {}: {}", notificationId, userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    // ============================================================
    // ❌ MARK NOTIFICATIONS AS UNREAD
    // ============================================================

    /**
     * Mark a single notification as unread
     * PUT /api/notifications/unread/{notificationId} - Mark a specific notification as unread
     */
    @PutMapping("/unread/{notificationId}")
    @Operation(summary = "Mark a single notification as unread",
               description = "Marks a specific notification as unread for the logged-in user. " +
                           "The notification must belong to the logged-in user. " +
                           "Optionally, you can specify a userId query parameter to mark notification for a specific user.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> markNotificationAsUnread(
            @RequestHeader HttpHeaders headers,
            @PathVariable Long notificationId,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            boolean marked = service.markNotificationAsUnread(notificationId, userId);
            
            if (marked) {
                return ResponseEntity.ok(new ResponseWrapper<>(
                        true,
                        "Notification marked as unread successfully",
                        Map.of("notificationId", notificationId, "userId", userId, "read", false)
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(
                                false,
                                "Notification not found, already unread, or does not belong to the user",
                                Map.of("notificationId", notificationId, "userId", userId, "read", false)
                        ));
            }
        } catch (Exception e) {
            log.error("❌ Failed to mark notification {} as unread: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Mark all notifications as unread for the logged-in user
     * PUT /api/notifications/unread-all - Mark all notifications as unread for current user
     */
    @PutMapping("/unread-all")
    @Operation(summary = "Mark all notifications as unread for logged-in user",
               description = "Marks all read notifications as unread for the logged-in user within configured days. " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30). " +
                           "Optionally, you can specify userId and days query parameters.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> markAllNotificationsAsUnread(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "userId", required = false) String userId) {
        
        try {
            // If userId is not provided, extract from JWT token
            if (userId == null || userId.isBlank()) {
                userId = extractUserIdFromToken(headers);
                if (userId == null) {
                    return ResponseEntity.status(401)
                            .body(new ResponseWrapper<>(false, "Unable to extract user ID from token. Please provide userId parameter or ensure valid JWT token.", null));
                }
            }

            int markedCount = service.markAllNotificationsAsUnread(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "All notifications marked as unread successfully",
                    Map.of("userId", userId, "markedCount", markedCount, "days", days != null ? days : 30)
            ));
        } catch (Exception e) {
            log.error("❌ Failed to mark all notifications as unread: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Mark all notifications as unread by userId (path parameter)
     * PUT /api/notifications/unread-all/{userId} - Mark all notifications as unread for a specific user
     */
    @PutMapping("/unread-all/{userId}")
    @Operation(summary = "Mark all notifications as unread by userId",
               description = "Marks all read notifications as unread for a specific user ID within configured days. " +
                           "The number of days can be configured in application.yml (notification.list.display-days, default: 30).")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> markAllNotificationsAsUnreadByUserId(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @RequestParam(value = "days", required = false) Integer days) {
        
        try {
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "User ID is required", null));
            }

            int markedCount = service.markAllNotificationsAsUnread(userId, days);
            
            return ResponseEntity.ok(new ResponseWrapper<>(
                    true,
                    "All notifications marked as unread successfully",
                    Map.of("userId", userId, "markedCount", markedCount, "days", days != null ? days : 30)
            ));
        } catch (Exception e) {
            log.error("❌ Failed to mark all notifications as unread for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

    /**
     * Extract userId from JWT token in Authorization header
     */
    private String extractUserIdFromToken(HttpHeaders headers) {
        try {
            String authHeader = headers.getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("⚠️ Missing or invalid Authorization header");
                return null;
            }

            String token = authHeader.substring(7);
            Claims claims = jwtVerifier.parseToken(token).getBody();
            
            // Try to get userId from subject or uid claim
            String userId = claims.getSubject();
            if (userId == null) {
                Object uidObj = claims.get("uid");
                if (uidObj != null) {
                    userId = uidObj.toString();
                }
            }
            
            return userId;
        } catch (Exception e) {
            log.warn("⚠️ Could not extract userId from token: {}", e.getMessage());
            return null;
        }
    }
}
