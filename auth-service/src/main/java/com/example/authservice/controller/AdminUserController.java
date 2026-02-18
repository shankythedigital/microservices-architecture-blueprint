package com.example.authservice.controller;

import com.example.authservice.dto.BlockUserRequest;
import com.example.authservice.dto.UserDto;
import com.example.authservice.service.UserService;
import com.example.authservice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserDto>> listUsers() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        List<UserDto> users = userService.listUsers(currentUserId);
        return ResponseEntity.ok(users);
    }

    /**
     * Temporarily block a user (Security, Compliance, PDPA/DPDPA). Admin only.
     * Body: { "reason": "...", "blockedUntil": "2025-03-01T00:00:00" } (blockedUntil optional)
     */
    @PostMapping("/{userId}/block")
    public ResponseEntity<Map<String, String>> blockUser(
            @PathVariable Long userId,
            @RequestBody(required = false) BlockUserRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        String reason = request != null && request.getReason() != null ? request.getReason() : "";
        userService.blockUser(userId, reason, request != null ? request.getBlockedUntil() : null, currentUserId);
        return ResponseEntity.ok(Map.of("status", "blocked", "message", "User temporarily blocked"));
    }

    /**
     * Unblock a temporarily blocked user. Admin only.
     */
    @PostMapping("/{userId}/unblock")
    public ResponseEntity<Map<String, String>> unblockUser(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        userService.unblockUser(userId, currentUserId);
        return ResponseEntity.ok(Map.of("status", "unblocked", "message", "User unblocked"));
    }

    /**
     * Permanently block a user. Reversal requires separate process (e.g. data protection request). Admin only.
     * Body: { "reason": "..." }
     */
    @PostMapping("/{userId}/permanent-block")
    public ResponseEntity<Map<String, String>> permanentBlockUser(
            @PathVariable Long userId,
            @RequestBody(required = false) BlockUserRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        String reason = request != null && request.getReason() != null ? request.getReason() : "";
        userService.permanentBlockUser(userId, reason, currentUserId);
        return ResponseEntity.ok(Map.of("status", "permanently_blocked", "message", "User permanently blocked"));
    }
}

