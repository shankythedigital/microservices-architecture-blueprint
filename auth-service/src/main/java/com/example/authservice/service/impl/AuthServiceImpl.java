package com.example.authservice.service.impl;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.model.*;
import com.example.authservice.repository.*;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.util.HmacUtil;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl {

    @Autowired private UserRepository userRepo;
    @Autowired private UserDetailMasterRepository udmRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private SessionRepository sessionRepo;
    @Autowired private RefreshTokenRepository refreshRepo;
    @Autowired private OtpLogRepository otpRepo;
    @Autowired private CredentialRepository credRepo;
    @Autowired private PendingResetRepository resetRepo;
    @Autowired private OtpServiceImpl otpService;
    @Autowired private AuditService auditService;
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // =====================================================
    // USER REGISTRATION
    // =====================================================
    public User register(String usernamePlain, String password, String emailPlain, String mobilePlain, String projectType) {
        if (usernamePlain == null || usernamePlain.isBlank())
            throw new IllegalArgumentException("Username is required");

        // Hash PII
        String usernameHash = HmacUtil.hmacHex(usernamePlain);
        String emailHash = emailPlain != null ? HmacUtil.hmacHex(emailPlain) : null;
        String mobileHash = mobilePlain != null ? HmacUtil.hmacHex(mobilePlain) : null;

        // Duplicate checks
        if (userRepo.existsByCompositeId_UsernameHash(usernameHash))
            throw new DuplicateKeyException("Username already exists");
        if (emailHash != null && udmRepo.existsByEmailHash(emailHash))
            throw new DuplicateKeyException("Email already exists");
        if (mobileHash != null && udmRepo.existsByMobileHash(mobileHash))
            throw new DuplicateKeyException("Mobile already exists");

        // Build composite key
        UserId compositeId = new UserId(null, usernameHash, emailHash, mobileHash, projectType);

        User user = new User();
        user.setCompositeId(compositeId);
        user.setUsernameEnc(usernamePlain);
        user.setPassword(password != null ? encoder.encode(password) : null);
        user.setEnabled(true);
        user.setCreatedBy("system");

        // Default role
        Role role = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
        user.setRoles(Set.of(role));

        // Create UserDetailMaster
        UserDetailMaster detail = new UserDetailMaster();
        detail.setUsername(usernamePlain);
        detail.setEmail(emailPlain);
        detail.setMobile(mobilePlain);
        detail.setCreatedBy("system");
        detail.setActive(true);
        detail.setUser(user);
        user.setDetail(detail);

        return userRepo.save(user);
    }

    public User adminregister(String usernamePlain, String password, String emailPlain, String mobilePlain, String projectType) {
        if (usernamePlain == null || usernamePlain.isBlank())
            throw new IllegalArgumentException("Username is required");

        String usernameHash = HmacUtil.hmacHex(usernamePlain);
        if (userRepo.existsByCompositeId_UsernameHash(usernameHash))
            throw new DuplicateKeyException("Username already exists");

        UserId compositeId = new UserId(null, usernameHash,
                emailPlain != null ? HmacUtil.hmacHex(emailPlain) : null,
                mobilePlain != null ? HmacUtil.hmacHex(mobilePlain) : null,
                projectType);

        User user = new User();
        user.setCompositeId(compositeId);
        user.setUsernameEnc(usernamePlain);
        user.setPassword(password != null ? encoder.encode(password) : null);
        user.setEnabled(true);
        user.setCreatedBy("system");

        Role role = roleRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));
        user.setRoles(Set.of(role));

        UserDetailMaster detail = new UserDetailMaster();
        detail.setUsername(usernamePlain);
        detail.setEmail(emailPlain);
        detail.setMobile(mobilePlain);
        detail.setCreatedBy("system");
        detail.setActive(true);
        detail.setUser(user);
        user.setDetail(detail);

        return userRepo.save(user);
    }

    // =====================================================
    // MPIN MANAGEMENT
    // =====================================================
    public void registerMpin(Long userId, String mpin, String deviceInfo) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String hash = encoder.encode(mpin);
        Credential c = credRepo.findByUser_UserIdAndTypeAndMetadata(userId, "MPIN", deviceInfo)
                .orElse(new Credential());
        c.setUser(user);
        c.setType("MPIN");
        c.setCredentialId("mpin-" + userId + "-" + deviceInfo);
        c.setPublicKey(hash);
        c.setMetadata(deviceInfo);
        c.setCreatedBy("system");
        credRepo.save(c);

        auditService.log(userId, "MPIN_REGISTER", "Credential", null, "****",
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    public String requestMpinReset(Long userId, String mobilePlain, String otp) {
        if (!otpService.validateOtp(mobilePlain, otp))
            throw new RuntimeException("Invalid OTP");

        String token = UUID.randomUUID().toString();
        PendingReset pr = new PendingReset();
        pr.setUserId(userId);
        pr.setType("MPIN");
        pr.setResetToken(token);
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetRepo.save(pr);

        auditService.log(userId, "MPIN_RESET_REQUEST", "Credential", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return token;
    }

    public void confirmMpinReset(String resetToken, String newMpin, String deviceInfo) {
        PendingReset pr = resetRepo.findByResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        if (pr.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset token expired");

        registerMpin(pr.getUserId(), newMpin, deviceInfo);
        resetRepo.delete(pr);

        auditService.log(pr.getUserId(), "MPIN_RESET_CONFIRM", "Credential", null, "****",
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    // =====================================================
    // EMAIL / MOBILE CHANGE
    // =====================================================
    public String requestEmailMobileChange(Long userId, String oldValue, String otp, String type) {
        if (!otpService.validateOtp(oldValue, otp))
            throw new RuntimeException("Invalid OTP for " + type);

        String token = UUID.randomUUID().toString();
        PendingReset pr = new PendingReset();
        pr.setUserId(userId);
        pr.setType(type);
        pr.setResetToken(token);
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetRepo.save(pr);

        auditService.log(userId, type + "_CHANGE_REQUEST", "UserDetailMaster", oldValue, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return token;
    }

    public void confirmEmailMobileChange(String resetToken, String newValue, String otp) {
        PendingReset pr = resetRepo.findByResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        if (pr.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset token expired");
        if (!otpService.validateOtp(newValue, otp))
            throw new RuntimeException("Invalid OTP for new value");

        UserDetailMaster udm = udmRepo.findById(pr.getUserId())
                .orElseThrow(() -> new RuntimeException("User detail not found"));
        if ("EMAIL".equalsIgnoreCase(pr.getType()))
            udm.setEmail(newValue);
        else if ("MOBILE".equalsIgnoreCase(pr.getType()))
            udm.setMobile(newValue);
        udmRepo.save(udm);
        resetRepo.delete(pr);

        auditService.log(pr.getUserId(), "CONTACT_CHANGE", "UserDetailMaster", null, newValue,
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    // =====================================================
    // LOGIN METHODS
    // =====================================================
    public AuthResponse loginWithPassword(String usernamePlain, String password, String deviceInfo) {
        if (usernamePlain == null || password == null)
            throw new IllegalArgumentException("Username and password are required");

        String usernameHash = HmacUtil.hmacHex(usernamePlain);
        User user = userRepo.findByCompositeId_UsernameHash(usernameHash)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getPassword() == null || !encoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid password");

        UserDetailMaster udm = udmRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User detail not found"));

        udm.setLastLoginDate(udm.getLoginDate());
        udm.setLoginDate(LocalDateTime.now());
        udm.setFailedAttempts(0);
        udmRepo.save(udm);

        AuthResponse resp = createSessionAndTokens(user, deviceInfo);
        auditService.log(user.getUserId(), "PASSWORD_LOGIN", "User", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return resp;
    }

    public AuthResponse loginWithOtp(String mobilePlain, String otp, String deviceInfo, String projectType) {
        String mobileHash = HmacUtil.hmacHex(mobilePlain);
        boolean ok = otpRepo.findAll().stream()
                .anyMatch(log -> mobileHash.equals(log.getMobileHash())
                        && !Boolean.TRUE.equals(log.isUsed())
                        && log.getExpiresAt().isAfter(LocalDateTime.now())
                        && HmacUtil.hmacHex(otp).equals(log.getOtpHash()));
        if (!ok) throw new RuntimeException("Invalid OTP");

        Optional<UserDetailMaster> od = udmRepo.findByMobileHash(mobileHash);
        User user = od.map(d -> userRepo.findById(d.getUserId()).orElseThrow())
                .orElseGet(() -> register(mobilePlain, null, null, mobilePlain, projectType));

        AuthResponse resp = createSessionAndTokens(user, deviceInfo);
        auditService.log(user.getUserId(), "OTP_LOGIN", "User", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return resp;
    }

    public AuthResponse loginWithMpin(Long userId, String mpin, String deviceInfo) {
        Credential cred = credRepo.findByUser_UserIdAndTypeAndMetadata(userId, "MPIN", deviceInfo)
                .orElseThrow(() -> new RuntimeException("MPIN not registered"));
        if (!encoder.matches(mpin, cred.getPublicKey()))
            throw new RuntimeException("Invalid MPIN");

        User user = userRepo.findById(userId).orElseThrow();
        AuthResponse resp = createSessionAndTokens(user, deviceInfo);
        auditService.log(userId, "MPIN_LOGIN", "User", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return resp;
    }

    // =====================================================
    // RSA + PASSKEY
    // =====================================================
    private final Map<Long, String> rsaChallenges = new HashMap<>();
    private final Map<Long, String> webauthnChallenges = new HashMap<>();

    public String createRsaChallenge(Long userId) {
        String challenge = UUID.randomUUID().toString();
        rsaChallenges.put(userId, challenge);
        return challenge;
    }

    public boolean verifyRsaSignature(Long userId, String challenge, String signatureBase64) {
        String expected = rsaChallenges.get(userId);
        if (expected == null || !expected.equals(challenge))
            return false;

        Optional<Credential> credOpt = credRepo.findByCredentialId("rsa-" + userId);
        if (credOpt.isEmpty()) return false;

        try {
            byte[] keyBytes = Base64.getDecoder().decode(credOpt.get().getPublicKey().replaceAll("\\n", ""));
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(expected.getBytes(StandardCharsets.UTF_8));
            boolean ok = sig.verify(Base64.getDecoder().decode(signatureBase64));
            if (ok) rsaChallenges.remove(userId);
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthResponse loginWithRsa(Long userId, String challenge, String signature, String deviceInfo) {
        if (!verifyRsaSignature(userId, challenge, signature))
            throw new RuntimeException("RSA verification failed");
        User user = userRepo.findById(userId).orElseThrow();
        return createSessionAndTokens(user, deviceInfo);
    }

    public Map<String, Object> webauthnChallenge(Long userId) {
        String challenge = UUID.randomUUID().toString();
        webauthnChallenges.put(userId, challenge);
        return Map.of("challenge", challenge, "userId", userId);
    }

    public boolean verifyWebauthnResponse(Long userId, String credentialId, String signature) {
        String challenge = webauthnChallenges.get(userId);
        if (challenge == null) return false;
        Optional<Credential> credOpt = credRepo.findByCredentialId(credentialId);
        if (credOpt.isEmpty()) return false;
        webauthnChallenges.remove(userId);
        return true;
    }

    public AuthResponse loginWithPasskey(Long userId, String credentialId, String signature, String deviceInfo) {
        if (!verifyWebauthnResponse(userId, credentialId, signature))
            throw new RuntimeException("Passkey verification failed");
        User user = userRepo.findById(userId).orElseThrow();
        return createSessionAndTokens(user, deviceInfo);
    }

    // =====================================================
    // CREDENTIAL MANAGEMENT
    // =====================================================
    public void registerCredential(Long userId, String type, String credentialId, String publicKey) {
        User user = userRepo.findById(userId).orElseThrow();
        Credential c = new Credential();
        c.setUser(user);
        c.setType(type);
        c.setCredentialId(credentialId);
        c.setPublicKey(publicKey);
        c.setCreatedBy("system");
        credRepo.save(c);
    }

    // =====================================================
    // TOKEN HANDLING
    // =====================================================
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null)
            throw new RuntimeException("Missing refresh token");

        String hash = HmacUtil.hmacHex(refreshToken);
        RefreshToken rt = refreshRepo.findByTokenHash(hash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Expired refresh token");

        Session s = rt.getSession();
        if (s == null || Boolean.TRUE.equals(s.isRevoked()))
            throw new RuntimeException("Invalid session");

        refreshRepo.delete(rt);

        String newRefresh = UUID.randomUUID().toString();
        RefreshToken nrt = new RefreshToken();
        nrt.setTokenHash(HmacUtil.hmacHex(newRefresh));
        nrt.setSession(s);
        nrt.setExpiryDate(LocalDateTime.now().plusDays(14));
        refreshRepo.save(nrt);

        List<String> roles = s.getUser().getRoles().stream().map(Role::getName).toList();
        String access = jwtUtil.generateAccessToken(s.getUser().getUserId(), s.getId(), roles);
        return new AuthResponse(access, newRefresh, jwtUtil.getAccessTokenValiditySeconds());
    }

    private AuthResponse createSessionAndTokens(User user, String deviceInfo) {
        Session session = new Session();
        session.setUser(user);
        session.setDeviceInfo(deviceInfo);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        sessionRepo.save(session);

        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String access = jwtUtil.generateAccessToken(user.getUserId(), session.getId(), roles);
        String refresh = UUID.randomUUID().toString();

        RefreshToken rt = new RefreshToken();
        rt.setTokenHash(HmacUtil.hmacHex(refresh));
        rt.setAccessToken(access);
        rt.setSession(session);
        rt.setExpiryDate(LocalDateTime.now().plusDays(14));
        refreshRepo.save(rt);

        return new AuthResponse(access, refresh, jwtUtil.getAccessTokenValiditySeconds());
    }

    // =====================================================
    // AUTH CODE
    // =====================================================
    public boolean validateAuthCode(Long userId, String authCode) {
        return "AUTH1234".equals(authCode);
    }
}


