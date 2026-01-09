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
