package com.example.authservice.service.impl;

import com.example.authservice.model.OtpLog;
import com.example.authservice.repository.OtpLogRepository;
import com.example.authservice.service.UserService;
import com.example.common.service.SafeNotificationHelper;
import com.example.common.util.HmacUtil;
import com.example.common.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ✅ OTP Service Implementation
 * Handles OTP generation, storage, validation, and notification.
 * Integrated with SafeNotificationHelper for reliable async notifications.
 */
@Service
public class OtpServiceImpl {

    private static final int EXPIRY_MINUTES = 3;

    @Autowired
    private OtpLogRepository otpLogRepository;

    @Autowired
    private SafeNotificationHelper safeNotificationHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * ✅ Generate and send OTP securely with bearer validation.
     */
    public String generateOtp(String userId,
                              String username,
                              String mobile,
                              String email,
                              String type,
                              String channel,
                              String templateCode,
                              String purpose,
                              String projectType) {

        // ------------------------------------------------------------------------
        // 1️⃣ Generate OTP
        // ------------------------------------------------------------------------
        String otpStr = String.format("%06d", new Random().nextInt(1_000_000));

        // ------------------------------------------------------------------------
        // 2️⃣ Persist OTP (hashed)
        // ------------------------------------------------------------------------
        OtpLog otp = new OtpLog();
        otp.setMobileHash(HmacUtil.hmacHex(mobile == null ? username : mobile));
        otp.setOtpHash(HmacUtil.hmacHex(otpStr));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        otp.setUsed(false);
        otp.setCreatedBy(username != null ? username : "system");
        otpLogRepository.save(otp);

        // ------------------------------------------------------------------------
        // 3️⃣ Prepare placeholders
        // ------------------------------------------------------------------------
        Map<String, Object> placeholders = new LinkedHashMap<>();
        placeholders.put("otp", otpStr);
        placeholders.put("purpose", purpose);

        // ------------------------------------------------------------------------
        // 4️⃣ Resolve user & token
        // ------------------------------------------------------------------------
        Long uid = null;
        try {
            uid = (userId != null && !userId.isBlank()) ? Long.parseLong(userId) : null;
        } catch (NumberFormatException ignored) {}

        String finalChannel = (channel != null && !channel.isBlank()) ? channel.toUpperCase() : "SMS";
        String finalTemplate = (templateCode != null && !templateCode.isBlank())
                ? templateCode
                : "OTP_" + finalChannel;
        String finalProject = (projectType != null) ? projectType : "AUTH_SERVICE";

        // ------------------------------------------------------------------------
        // 5️⃣ Resolve bearer token (required)
        // ------------------------------------------------------------------------
        String token = userService
                .getLatestAccessToken(RequestContext.getSessionId(), username, uid)
                .orElse(null);

        if (token == null || token.isBlank()) {
            String message = String.format("❌ Missing or invalid bearer token while generating OTP for user=%s", username);
            System.err.println(message);
            throw new RuntimeException(message);
        }

        String bearer = token.startsWith("Bearer ") ? token : "Bearer " + token;

        // ------------------------------------------------------------------------
        // 6️⃣ Send main notification (required)
        // ------------------------------------------------------------------------
        try {
            safeNotificationHelper.safeNotify(
                    bearer,
                    uid,
                    username,
                    email,
                    mobile,
                    finalChannel,
                    finalTemplate,
                    placeholders,
                    finalProject
            );

            System.out.printf("📤 OTP notification sent via %s for user=%s (%s)%n",
                    finalChannel, username, finalProject);

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to send main OTP notification: " + e.getMessage(), e);
        }

        // ------------------------------------------------------------------------
        // 7️⃣ Optional fallback (secondary channel)
        // ------------------------------------------------------------------------
        try {
            if ("SMS".equalsIgnoreCase(finalChannel) && email != null) {
                safeNotificationHelper.safeNotifyAsync(
                        bearer, uid, username, email, mobile,
                        "EMAIL", "OTP_EMAIL", placeholders, finalProject
                );
                System.out.printf("📧 Fallback EMAIL OTP sent to %s%n", email);
            } else if ("EMAIL".equalsIgnoreCase(finalChannel) && mobile != null) {
                safeNotificationHelper.safeNotifyAsync(
                        bearer, uid, username, email, mobile,
                        "SMS", "OTP_SMS", placeholders, finalProject
                );
                System.out.printf("📱 Fallback SMS OTP sent to %s%n", mobile);
            }
        } catch (Exception ex) {
            System.err.printf("⚠️ OTP fallback notification failed: %s%n", ex.getMessage());
        }

        // ------------------------------------------------------------------------
        // 8️⃣ Audit Log
        // ------------------------------------------------------------------------
        System.out.printf("✅ OTP %s generated successfully for %s via %s (purpose=%s)%n",
                otpStr, username, finalChannel, purpose);

        return otpStr;
    }

    /**
     * ✅ Validate OTP input against stored entry.
     */
    public boolean validateOtp(String mobile, String otpInput) {
        String mobileHash = HmacUtil.hmacHex(mobile);

        OtpLog otp = otpLogRepository
                .findTopByMobileHashAndUsedFalseOrderByCreatedAtDesc(mobileHash)
                .orElseThrow(() -> new RuntimeException("No OTP found or expired"));

        if (otp.isUsed() || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            System.err.printf("⚠️ OTP expired or already used for mobile=%s%n", mobile);
            return false;
        }

        boolean valid = HmacUtil.hmacHex(otpInput).equals(otp.getOtpHash());
        if (valid) {
            otp.setUsed(true);
            otpLogRepository.save(otp);
            System.out.printf("✅ OTP validated successfully for mobile=%s%n", mobile);
        } else {
            System.err.printf("❌ Invalid OTP attempt for mobile=%s%n", mobile);
        }

        return valid;
    }
}



