package com.example.authservice.service.impl;

import com.example.authservice.client.NotificationClient;
import com.example.authservice.model.OtpLog;
import com.example.authservice.repository.OtpLogRepository;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.service.UserService;
import com.example.authservice.util.HmacUtil;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ✅ OTP Service Implementation
 * Handles OTP generation, persistence, validation, and cross-channel notification dispatch.
 */
@Service
public class OtpServiceImpl {

    private static final int EXPIRY_MINUTES = 3;

    @Autowired
    private OtpLogRepository otpLogRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * ✅ Generate OTP, persist in DB, and send via notification channels
     *
     * @param userId       User ID (can be null or 0 for change requests)
     * @param username     Username or identifier
     * @param mobile       Mobile number
     * @param email        Email address
     * @param type         Primary type of OTP trigger (SMS/EMAIL/WHATSAPP)
     * @param channel      Notification channel
     * @param purpose      OTP purpose (LOGIN, CHANGE, etc.)
     * @return Generated OTP code
     */
    public String generateOtp(String userId, String username, String mobile,
                              String email, String type, String channel, String purpose) {

        StringBuilder errorMessage = new StringBuilder();
        String otpStr = String.format("%06d", new Random().nextInt(1_000_000));

        // -----------------------------------
        // 1️⃣ Persist OTP in DB
        // -----------------------------------
        OtpLog log = new OtpLog();
        log.setMobileHash(HmacUtil.hmacHex(mobile == null ? username : mobile));
        log.setOtpHash(HmacUtil.hmacHex(otpStr));
        log.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        log.setUsed(false);
        log.setCreatedBy(username != null ? username : "system");
        otpLogRepository.save(log);

        // -----------------------------------
        // 2️⃣ Prepare placeholders & audit
        // -----------------------------------
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("otp", otpStr);
        placeholders.put("purpose", purpose);

        Map<String, Object> audit = new HashMap<>();
        audit.put("ipAddress", RequestContext.getIp());
        audit.put("userAgent", RequestContext.getUserAgent());
        audit.put("url", "/api/notifications");
        audit.put("httpMethod", "POST");

        // -----------------------------------
        // 3️⃣ Fetch Access Token (if available)
        // -----------------------------------
        Long uid = null;
        try {
            uid = (userId != null) ? Long.valueOf(userId) : null;
        } catch (Exception ignored) {}

        String token = null;
        if (uid != null && uid > 0) {
            token = userService.getLatestAccessTokenByUserId(uid).orElse(null);
        }

        // -----------------------------------
        // 4️⃣ Determine Template Code Dynamically
        // -----------------------------------
        String templateCode = "OTP_" + (channel != null ? channel.toUpperCase() : "SMS");

        // -----------------------------------
        // 5️⃣ Send OTP Notification(s)
        // -----------------------------------
        try {
            // Primary channel notification (e.g., SMS / EMAIL)
            sendNotification(token, userId, username, mobile, email, channel, templateCode, placeholders, audit);

            // If channel supports secondary fallback (optional logic)
            if (!"INAPP".equalsIgnoreCase(channel)) {
                if ("SMS".equalsIgnoreCase(channel) && email != null) {
                    sendNotification(token, userId, username, mobile, email, "EMAIL", "OTP_EMAIL", placeholders, audit);
                } else if ("EMAIL".equalsIgnoreCase(channel) && mobile != null) {
                    sendNotification(token, userId, username, mobile, email, "SMS", "OTP_SMS", placeholders, audit);
                }
            }

        } catch (Exception ex) {
            errorMessage.append("\n❌ Notification send failed: ").append(ex.getMessage());
        }

        // -----------------------------------
        // 6️⃣ Error Handling
        // -----------------------------------
        if (errorMessage.length() > 0) {
            throw new RuntimeException(errorMessage.toString());
        }

        System.out.println("✅ OTP " + otpStr + " generated for " + username + " (" + channel + ")");
        return otpStr;
    }

    /**
     * ✅ Send notification through Feign client
     */
    private void sendNotification(String token,
                                  String userId,
                                  String username,
                                  String mobile,
                                  String email,
                                  String channel,
                                  String templateCode,
                                  Map<String, Object> placeholders,
                                  Map<String, Object> audit) {
        try {
            Map<String, Object> req = new LinkedHashMap<>();
            req.put("userId", userId);
            req.put("username", username);
            req.put("mobile", mobile);
            req.put("email", email);
            req.put("channel", channel.toUpperCase());
            req.put("templateCode", templateCode);
            req.put("placeholders", placeholders);
            req.put("audit", audit);

            String bearer = (token != null && !token.isBlank())
                    ? (token.startsWith("Bearer ") ? token : "Bearer " + token)
                    : null;

            notificationClient.sendNotification(req, bearer);
            System.out.println("📤 Notification sent via " + channel + " → " + username);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to send " + channel + " notification: " + ex.getMessage(), ex);
        }
    }

    /**
     * ✅ Validate OTP input against latest unused DB entry
     */
    public boolean validateOtp(String mobile, String otpInput) {
        String mobileHash = HmacUtil.hmacHex(mobile);

        OtpLog log = otpLogRepository.findTopByMobileHashAndUsedFalseOrderByCreatedAtDesc(mobileHash)
                .orElseThrow(() -> new RuntimeException("No OTP found or expired"));

        if (log.isUsed() || log.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        boolean valid = HmacUtil.hmacHex(otpInput).equals(log.getOtpHash());
        if (valid) {
            log.setUsed(true);
            otpLogRepository.save(log);
        }
        return valid;
    }
}



