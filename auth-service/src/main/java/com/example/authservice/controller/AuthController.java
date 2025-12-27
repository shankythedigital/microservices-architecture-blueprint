

package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.impl.AuthServiceImpl;
import com.example.authservice.service.impl.OtpServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

import com.example.authservice.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private OtpServiceImpl otpService;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            authService.register(req.username, req.password, req.email, req.mobile, req.projectType);
            return ResponseEntity.ok("registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/adminregister")
    public ResponseEntity<?> adminregister(@RequestBody RegisterRequest req) {
        try {
            authService.adminregister(req.username, req.password, req.email, req.mobile, req.projectType);
            return ResponseEntity.ok("registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            switch (req.loginType) {
                case "PASSWORD":
                    return ResponseEntity.ok(authService.loginWithPassword(req.username, req.password, req.deviceInfo));
                case "OTP":
                    return ResponseEntity.ok(authService.loginWithOtp(req.username, req.otp, req.deviceInfo, "ECOM"));
                case "MPIN":
                    return ResponseEntity
                            .ok(authService.loginWithMpin(Long.parseLong(req.username), req.mpin, req.deviceInfo));
                case "RSA":
                    return ResponseEntity.ok(authService.loginWithRsa(Long.parseLong(req.username), req.rsaChallenge,
                            req.signature, req.deviceInfo));
                case "PASSKEY":
                    return ResponseEntity.ok(authService.loginWithPasskey(Long.parseLong(req.username),
                            req.credentialId, req.signature, req.deviceInfo));
                case "AUTHCODE":
                    if (authService.validateAuthCode(Long.parseLong(req.username), req.authCode)) {
                        return ResponseEntity.ok(authService.loginWithPassword(req.username, "dummy", req.deviceInfo));
                    } else {
                        return ResponseEntity.status(401).body("Invalid auth code");
                    }
                default:
                    return ResponseEntity.badRequest().body("Unsupported loginType");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // POST /credential/rsa/verify
    @PostMapping("/credential/rsa/verify")
    public ResponseEntity<?> rsaVerify(@Valid @RequestBody RsaVerifyRequest req) {
        boolean ok = authService.verifyRsaSignature(req.userId, req.challenge, req.signature);
        if (ok) {
            return ResponseEntity.ok("RSA signature verified successfully");
        } else {
            return ResponseEntity.status(401).body("Invalid RSA signature");
        }
    }

    // WebAuthn: verify (keeps using generic DTO)
    @PostMapping("/credential/webauthn/verify")
    public ResponseEntity<?> webauthnVerify(@Valid @RequestBody CredentialChallengeResponse req) {
        boolean ok = authService.verifyWebauthnResponse(req.userId, req.credentialId, req.signature);
        if (ok) {
            return ResponseEntity.ok(Map.of("userId", req.userId, "verified", true));
        } else {
            return ResponseEntity.status(401)
                    .body(Map.of("userId", req.userId, "verified", false, "message", "Invalid WebAuthn response"));
        }
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            // -----------------------------------
            // 1️⃣ Extract request body fields
            // -----------------------------------
            Long userId = payload.get("userId") != null ? Long.parseLong(payload.get("userId").toString()) : 0L;
            String channel = (String) payload.getOrDefault("channel", "SMS");
            String templateCode = (String) payload.getOrDefault("templateCode", "OTP_SMS");
            String mobile = payload.get("mobile") != null ? payload.get("mobile").toString() : null;
            String email = payload.get("email") != null ? payload.get("email").toString() : null;
            String projecttype = payload.get("projecttype") != null ? payload.get("projecttype").toString() : null;
            String type = (String) payload.getOrDefault("type", "SMS");
            String purpose = (String) payload.getOrDefault("purpose", "LOGIN");

            // -----------------------------------
            // 2️⃣ Validation of required fields
            // -----------------------------------
            if (type == null || type.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Type is required (SMS, EMAIL, WHATSAPP)"));
            }

            if (("SMS".equalsIgnoreCase(type) || "WHATSAPP".equalsIgnoreCase(type))
                    && (mobile == null || mobile.isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mobile number is required for " + type));
            }

            if ("EMAIL".equalsIgnoreCase(type) && (email == null || email.isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required for EMAIL type"));
            }

            // -----------------------------------
            // 3️⃣ Resolve username based on userId or identifier
            // -----------------------------------
            String identifier = (mobile != null && !mobile.isBlank()) ? mobile : email;
            String username;

            if ("LOGIN".equalsIgnoreCase(purpose)) {
                username = userService.getUsernameFromIdentifier(identifier, type);
                if (username == null) {
                    return ResponseEntity.status(404)
                            .body(Map.of("error", "User not found for identifier: " + identifier));
                }
            } else if ("CHANGE".equalsIgnoreCase(purpose)) {
                username = "change-" + identifier;
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Unsupported purpose. Use LOGIN or CHANGE."));
            }

            // -----------------------------------
            // 4️⃣ Validate formats
            // -----------------------------------
            if (mobile != null && !mobile.matches("^[0-9]{10,15}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid mobile number format"));
            }
            if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email address format"));
            }

            // -----------------------------------
            // 5️⃣ Generate and send OTP (delegates to OtpServiceImpl)
            // -----------------------------------
            String otp = otpService.generateOtp(
                    userId.toString(),
                    username,
                    mobile,
                    email,
                    type,
                    channel,
                    templateCode, // Notice: templateCode instead of purpose
                    purpose, 
                    projecttype
            );

            // -----------------------------------
            // 6️⃣ Build and return response
            // -----------------------------------
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "OTP sent successfully via " + type);
            response.put("userId", userId);
            response.put("mobile", mobile);
            response.put("email", email);
            response.put("channel", channel);
            response.put("templateCode", templateCode);
            response.put("purpose", purpose.toUpperCase());
            response.put("otp", otp); // ⚠️ For dev/debug only — remove in production
            response.put("expiresInMinutes", 3);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        try {
            return ResponseEntity.ok(authService.refresh(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/credential/register")
    public ResponseEntity<?> registerCredential(@RequestBody CredentialRegisterRequest req) {
        try {
            authService.registerCredential(req.userId, req.type, req.credentialId, req.publicKey);
            return ResponseEntity.ok("Credential registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/credential/rsa/challenge/{userId}")
    public ResponseEntity<?> rsaChallenge(@PathVariable Long userId) {
        String challenge = authService.createRsaChallenge(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "challenge", challenge));
    }

    @GetMapping("/credential/webauthn/challenge/{userId}")
    public ResponseEntity<?> webauthnChallenge(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.webauthnChallenge(userId));
    }

    @PostMapping("/mpin/register")
    public ResponseEntity<?> registerMpin(@RequestBody MpinRegisterRequest req) {
        try {
            authService.registerMpin(req.userId, req.mpin, req.deviceInfo);
            return ResponseEntity.ok("MPIN registered/reset");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/mpin/verify")
    public ResponseEntity<?> verifyMpin(@RequestBody MpinVerifyRequest req) {
        try {
            return ResponseEntity.ok(authService.loginWithMpin(req.userId, req.mpin, req.deviceInfo));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/mpin/reset/request")
    public ResponseEntity<?> requestMpinReset(@RequestBody MpinResetRequest req) {
        try {
            String token = authService.requestMpinReset(req.userId, req.mobile, req.otp);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/mpin/reset/confirm")
    public ResponseEntity<?> confirmMpinReset(@RequestBody MpinResetConfirmRequest req) {
        try {
            authService.confirmMpinReset(req.resetToken, req.newMpin, req.deviceInfo);
            return ResponseEntity.ok("MPIN reset successful");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/contact/change/request")
    public ResponseEntity<?> requestChange(@RequestBody EmailMobileChangeRequest req) {
        try {
            String token = authService.requestEmailMobileChange(req.userId, req.oldValue, req.otp, req.type);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/contact/change/confirm")
    public ResponseEntity<?> confirmChange(@RequestBody EmailMobileChangeConfirmRequest req) {
        try {
            authService.confirmEmailMobileChange(req.resetToken, req.newValue, req.otp);
            return ResponseEntity.ok("Change successful");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}


