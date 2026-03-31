
package com.example.authservice.service;

import com.example.authservice.dto.UserDto;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.model.User;
import com.example.authservice.model.UserDetailMaster;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserDetailMasterRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.impl.AuditService;
import com.example.authservice.service.impl.AuthServiceImpl;
import com.example.common.util.FileStorageUtil;
import com.example.common.util.EncryptDecryptUtil;
import com.example.common.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepo;
    @Autowired private UserDetailMasterRepository udmRepo;
    @Autowired private RefreshTokenRepository refreshRepo;
    @Autowired private UserMapper userMapper;
    @Autowired private FileStorageUtil fileStorageUtil;
    @Autowired private AuditService auditService;
    @Autowired private AuthServiceImpl authService;

    public UserDto getMyProfile(Long currentUserId) {
        if (currentUserId == null) throw new RuntimeException("Unauthorized: No active user context");

        User user = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + currentUserId));
        if (user.getDeletedAt() != null)
            throw new RuntimeException("Account has been deleted");

        UserDetailMaster udm = udmRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User details not found for ID: " + currentUserId));

        return userMapper.toDto(user, udm);
    }

    public UserDto getUserProfile(Long targetUserId, Long currentUserId) {
        if (targetUserId == null || currentUserId == null) throw new RuntimeException("Invalid request");

        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new RuntimeException("Access denied: not authorized to view another user's profile");
        }

        User target = userRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        if (target.getDeletedAt() != null)
            throw new RuntimeException("Account has been deleted");
        UserDetailMaster udm = udmRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User details not found"));

        return userMapper.toDto(target, udm);
    }

    public java.util.List<com.example.authservice.dto.UserDto> listUsers(Long currentUserId) {
        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin) throw new RuntimeException("Access denied: only admins can list all users");

        var users = userRepo.findAll();
        var out = new java.util.ArrayList<UserDto>();
        for (User u : users) {
            if (u.getDeletedAt() != null) continue; // Exclude soft-deleted users
            UserDetailMaster udm = udmRepo.findByUserId(u.getUserId()).orElse(null);
            out.add(userMapper.toDto(u, udm));
        }
        return out;
    }

    public Long resolveUserId(String identifier, String type) {
        if (identifier == null || identifier.isBlank()) return null;
        String hash = EncryptDecryptUtil.hmac(identifier);
        if (type != null) {
            switch (type.toUpperCase()) {
                case "USERNAME":
                    Optional<UserDetailMaster> optU = udmRepo.findByUsernameHash(hash);
                    return optU.map(UserDetailMaster::getUserId).orElse(null);
                case "EMAIL":
                    Optional<UserDetailMaster> optE = udmRepo.findByEmailHash(hash);
                    return optE.map(UserDetailMaster::getUserId).orElse(null);
                case "SMS":
                    Optional<UserDetailMaster> optM = udmRepo.findByMobileHash(hash);
                    return optM.map(UserDetailMaster::getUserId).orElse(null);
                default:
                    throw new RuntimeException("Invalid type parameter. Must be one of: username, email, mobile");
            }
        }
        return null; // Ensure a Long is returned
        // Optional<UserDetailMaster> opt = udmRepo.findByUsernameHash(hash);
        // if (opt.isPresent()) return opt.get().getUserId();
        // opt = udmRepo.findByEmailHash(hash);
        // if (opt.isPresent()) return opt.get().getUserId();
        // opt = udmRepo.findByMobileHash(hash);
        // if (opt.isPresent()) return opt.get().getUserId();
        // return null;
    }

    public String getUsernameFromIdentifier(String identifier,String type) {
        if (identifier == null || identifier.isBlank()) return null;
        String hash = EncryptDecryptUtil.hmac(identifier);

        if (type != null) {
            switch (type.toUpperCase()) {
                case "USERNAME":
                    Optional<UserDetailMaster> optU = udmRepo.findByUsernameHash(hash);
                    return optU.map(UserDetailMaster::getUsername).orElse(null);
                case "EMAIL":
                    Optional<UserDetailMaster> optE = udmRepo.findByEmailHash(hash);
                    return optE.map(UserDetailMaster::getUsername).orElse(null);
                case "SMS":
                    Optional<UserDetailMaster> optM = udmRepo.findByMobileHash(hash);
                    return optM.map(UserDetailMaster::getUsername).orElse(null);
                default:
                    throw new RuntimeException("Invalid type parameter. Must be one of: username, email, mobile");
            }
        }
        // Optional<UserDetailMaster> opt = udmRepo.findByUsernameHash(hash);
        // if (opt.isPresent()) return opt.get().getUsername();
        // opt = udmRepo.findByEmailHash(hash);
        // if (opt.isPresent()) return opt.get().getUsername();
        // opt = udmRepo.findByMobileHash(hash);
        // if (opt.isPresent()) return opt.get().getUsername();
        return null;
    }
    // =====================================================
    // PATCH: Get Latest Access Token by sessionId, username, or userId
    // =====================================================

    /**
    * Fetch the latest valid access token based on the strongest unique context available:
    * Priority: sessionId → username → userId → global fallback.
    */
    public Optional<String> getLatestAccessToken(Long sessionId, String username, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1️⃣ Lookup by Session ID — always unique
            if (sessionId != null && sessionId > 0) {
                return refreshRepo
                        .findTopBySession_IdAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(sessionId, now)
                        .map(token -> {
                            System.out.println("✅ [UserService] Found token by sessionId=" + sessionId);
                            return token.getAccessToken();
                        });
            }

            // 2️⃣ Lookup by Username — unique within project scope
            if (username != null && !username.isBlank()) {
                var tokenOpt = refreshRepo
                        .findTopBySession_User_Detail_UsernameIgnoreCaseAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(username, now)
                        .map(token -> {
                            System.out.println("✅ [UserService] Found token by username=" + username);
                            return token.getAccessToken();
                        });
                if (tokenOpt.isPresent()) return tokenOpt;
            }

            // 3️⃣ Lookup by User ID — globally unique per user
            if (userId != null && userId > 0) {
                var tokenOpt = refreshRepo
                        .findTopBySession_User_UserIdAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(userId, now)
                        .map(token -> {
                            System.out.println("✅ [UserService] Found token by userId=" + userId);
                            return token.getAccessToken();
                        });
                if (tokenOpt.isPresent()) return tokenOpt;
            }

            // 4️⃣ Fallback — any global valid token (use with caution)
            return refreshRepo
                    .findTopByActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(now)
                    .map(token -> {
                        System.out.println("⚠️ [UserService] Fallback to latest global valid token");
                        return token.getAccessToken();
                    });

        } catch (Exception ex) {
            System.err.println("❌ [UserService] Token lookup failed: " + ex.getMessage());
        }

        return Optional.empty();
    }

    // =====================================================
    // USER PROFILE MANAGEMENT
    // =====================================================

    /**
     * Get user profile by userId (returns UserProfileResponse with extended profile fields)
     * Users can only view their own profile unless they are admin
     */
    public com.example.authservice.dto.UserProfileResponse getUserProfileExtended(Long userId, Long currentUserId) {
        if (userId == null || currentUserId == null) {
            throw new RuntimeException("Invalid request: userId and currentUserId are required");
        }

        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !userId.equals(currentUserId)) {
            throw new RuntimeException("Access denied: not authorized to view another user's profile");
        }

        User targetUser = userRepo.findByUserId(userId).orElse(null);
        if (targetUser != null && targetUser.getDeletedAt() != null)
            throw new RuntimeException("Account has been deleted");

        UserDetailMaster udm = udmRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + userId));

        return mapToProfileResponse(udm);
    }

    /**
     * Get a single decrypted field for a user. Call once per field.
     * User: own fields only. Admin: any user's fields.
     * Allowed fields: username, email, mobile, firstName, lastName, employeeId, pincode, city, state,
     * country, address1, address2, address3, countryCode, dateOfBirth, gender, occupation, education,
     * maritalStatus, profilePhotoUrl, linkedinUrl, facebookUrl, twitterUrl, instagramUrl, githubUrl,
     * websiteUrl, preferences, activityPatterns, interests, bio, skills, languages, timezone, additionalInfo.
     */
    public com.example.authservice.dto.DecryptFieldResponse getDecryptedField(Long targetUserId, String fieldName, Long currentUserId) {
        if (targetUserId == null || currentUserId == null || fieldName == null || fieldName.isBlank()) {
            throw new RuntimeException("Invalid request: userId, currentUserId and field are required");
        }

        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r != null && r.getName() != null && r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new RuntimeException("Access denied: not authorized to view another user's data");
        }

        User targetUser = userRepo.findByUserId(targetUserId).orElse(null);
        if (targetUser != null && targetUser.getDeletedAt() != null) {
            throw new RuntimeException("Account has been deleted");
        }

        UserDetailMaster udm = udmRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + targetUserId));

        String value = getFieldValue(udm, fieldName.trim());
        return new com.example.authservice.dto.DecryptFieldResponse(fieldName.trim(), value);
    }

    private String getFieldValue(UserDetailMaster udm, String field) {
        switch (field.toLowerCase()) {
            case "username": return udm.getUsername();
            case "email": return udm.getEmail();
            case "mobile": return udm.getMobile();
            case "firstname": return udm.getFirstName();
            case "lastname": return udm.getLastName();
            case "employeeid": return udm.getEmployeeId();
            case "pincode": return udm.getPincode();
            case "city": return udm.getCity();
            case "state": return udm.getState();
            case "country": return udm.getCountry();
            case "address1": return udm.getAddress1();
            case "address2": return udm.getAddress2();
            case "address3": return udm.getAddress3();
            case "countrycode": return udm.getCountryCode();
            case "dateofbirth": return udm.getDateOfBirth();
            case "gender": return udm.getGender();
            case "occupation": return udm.getOccupation();
            case "education": return udm.getEducation();
            case "maritalstatus": return udm.getMaritalStatus();
            case "profilephotourl": return udm.getProfilePhotoUrl();
            case "linkedinurl": return udm.getLinkedinUrl();
            case "facebookurl": return udm.getFacebookUrl();
            case "twitterurl": return udm.getTwitterUrl();
            case "instagramurl": return udm.getInstagramUrl();
            case "githuburl": return udm.getGithubUrl();
            case "websiteurl": return udm.getWebsiteUrl();
            case "preferences": return udm.getPreferences();
            case "activitypatterns": return udm.getActivityPatterns();
            case "interests": return udm.getInterests();
            case "bio": return udm.getBio();
            case "skills": return udm.getSkills();
            case "languages": return udm.getLanguages();
            case "timezone": return udm.getTimezone();
            case "additionalinfo": return udm.getAdditionalInfo();
            default: throw new RuntimeException("Unknown field: " + field + ". Allowed: username, email, mobile, firstName, lastName, employeeId, pincode, city, state, country, address1-3, countryCode, dateOfBirth, gender, occupation, education, maritalStatus, profilePhotoUrl, linkedinUrl, facebookUrl, twitterUrl, instagramUrl, githubUrl, websiteUrl, preferences, activityPatterns, interests, bio, skills, languages, timezone, additionalInfo");
        }
    }

    /**
     * Get only communication opt-out preferences for a user (for notification senders).
     * Normal user: own preferences only; admin: any user's preferences.
     */
    public com.example.authservice.dto.CommunicationPreferencesDto getCommunicationPreferences(Long userId, Long currentUserId) {
        if (userId == null || currentUserId == null)
            throw new RuntimeException("userId and currentUserId are required");
        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        boolean isAdmin = current.getRoles() != null && current.getRoles().stream()
                .anyMatch(r -> r != null && r.getName() != null && r.getName().equalsIgnoreCase("ROLE_ADMIN"));
        if (!isAdmin && !userId.equals(currentUserId))
            throw new RuntimeException("Access denied: not authorized to view another user's communication preferences");
        User targetUser = userRepo.findByUserId(userId).orElse(null);
        if (targetUser != null && targetUser.getDeletedAt() != null)
            throw new RuntimeException("Account has been deleted");
        UserDetailMaster udm = udmRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + userId));
        com.example.authservice.dto.CommunicationPreferencesDto dto = new com.example.authservice.dto.CommunicationPreferencesDto();
        dto.setOptOutSms(Boolean.TRUE.equals(udm.getOptOutSms()));
        dto.setOptOutEmail(Boolean.TRUE.equals(udm.getOptOutEmail()));
        dto.setOptOutWhatsapp(Boolean.TRUE.equals(udm.getOptOutWhatsapp()));
        dto.setOptOutInapp(Boolean.TRUE.equals(udm.getOptOutInapp()));
        dto.setOptOutPush(Boolean.TRUE.equals(udm.getOptOutPush()));
        return dto;
    }

    /**
     * Reject profile update if request contains any restricted field (username, email, mobile, acceptTc).
     * userId is not updatable (determined by path).
     */
    private void validateNoRestrictedProfileFields(com.example.authservice.dto.UserProfileRequest request) {
        if (request == null) return;
        if (request.getUsername() != null || request.getEmail() != null
                || request.getMobile() != null || request.getAcceptTc() != null) {
            throw new RuntimeException(
                    "Username, email, mobile and terms acceptance cannot be updated via profile update. Use the dedicated flows for contact change and T&C.");
        }
    }

    /**
     * Update user profile (extended profile fields including address).
     * <ul>
     *   <li>Users can update their own profile (address1, address2, address3, pincode, city, state, country, etc.).</li>
     *   <li>Admins can update any user's profile.</li>
     *   <li>Restricted fields (cannot be updated by anyone via this endpoint): userId, username, email, mobile, acceptTc.</li>
     * </ul>
     */
    public com.example.authservice.dto.UserProfileResponse updateUserProfileExtended(
            Long userId, 
            Long currentUserId, 
            com.example.authservice.dto.UserProfileRequest request) {
        
        if (userId == null || currentUserId == null) {
            throw new RuntimeException("Invalid request: userId and currentUserId are required");
        }

        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = current.getRoles() != null && current.getRoles().stream()
                .anyMatch(r -> r != null && r.getName() != null && r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !userId.equals(currentUserId)) {
            throw new RuntimeException("Access denied: not authorized to update another user's profile");
        }

        validateNoRestrictedProfileFields(request);

        UserDetailMaster udm = udmRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + userId));

        // Handle profile photo update - delete old photo if new one is provided
        if (request.getFirstName() != null) {
            udm.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            udm.setLastName(request.getLastName());
        }
        if (request.getProfilePhotoUrl() != null) {
            // Delete old photo if it exists and is different from the new one
            String oldPhotoUrl = udm.getProfilePhotoUrl();
            if (oldPhotoUrl != null && !oldPhotoUrl.equals(request.getProfilePhotoUrl()) && 
                oldPhotoUrl.startsWith("uploads/USER_PROFILE/")) {
                try {
                    fileStorageUtil.deleteFile(oldPhotoUrl);
                } catch (Exception e) {
                    // Log but don't fail the update if photo deletion fails
                    System.err.println("⚠️ Failed to delete old profile photo: " + e.getMessage());
                }
            }
            udm.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        if (request.getPincode() != null) {
            udm.setPincode(request.getPincode());
        }
        if (request.getCity() != null) {
            udm.setCity(request.getCity());
        }
        if (request.getState() != null) {
            udm.setState(request.getState());
        }
        if (request.getCountry() != null) {
            udm.setCountry(request.getCountry());
        }
        if (request.getCountryCode() != null) {
            udm.setCountryCode(request.getCountryCode());
        }
        if (request.getAddress1() != null) {
            udm.setAddress1(request.getAddress1());
        }
        if (request.getAddress2() != null) {
            udm.setAddress2(request.getAddress2());
        }
        if (request.getAddress3() != null) {
            udm.setAddress3(request.getAddress3());
        }
        if (request.getLinkedinUrl() != null) {
            udm.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getFacebookUrl() != null) {
            udm.setFacebookUrl(request.getFacebookUrl());
        }
        if (request.getTwitterUrl() != null) {
            udm.setTwitterUrl(request.getTwitterUrl());
        }
        if (request.getInstagramUrl() != null) {
            udm.setInstagramUrl(request.getInstagramUrl());
        }
        if (request.getGithubUrl() != null) {
            udm.setGithubUrl(request.getGithubUrl());
        }
        if (request.getWebsiteUrl() != null) {
            udm.setWebsiteUrl(request.getWebsiteUrl());
        }
        if (request.getDateOfBirth() != null) {
            udm.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            udm.setGender(request.getGender());
        }
        if (request.getOccupation() != null) {
            udm.setOccupation(request.getOccupation());
        }
        if (request.getEducation() != null) {
            udm.setEducation(request.getEducation());
        }
        if (request.getMaritalStatus() != null) {
            udm.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getPreferences() != null) {
            udm.setPreferences(request.getPreferences());
        }
        if (request.getActivityPatterns() != null) {
            udm.setActivityPatterns(request.getActivityPatterns());
        }
        if (request.getInterests() != null) {
            udm.setInterests(request.getInterests());
        }
        if (request.getBio() != null) {
            udm.setBio(request.getBio());
        }
        if (request.getSkills() != null) {
            udm.setSkills(request.getSkills());
        }
        if (request.getLanguages() != null) {
            udm.setLanguages(request.getLanguages());
        }
        if (request.getTimezone() != null) {
            udm.setTimezone(request.getTimezone());
        }
        if (request.getAdditionalInfo() != null) {
            udm.setAdditionalInfo(request.getAdditionalInfo());
        }
        if (request.getOptOutSms() != null) {
            udm.setOptOutSms(request.getOptOutSms());
        }
        if (request.getOptOutEmail() != null) {
            udm.setOptOutEmail(request.getOptOutEmail());
        }
        if (request.getOptOutWhatsapp() != null) {
            udm.setOptOutWhatsapp(request.getOptOutWhatsapp());
        }
        if (request.getOptOutInapp() != null) {
            udm.setOptOutInapp(request.getOptOutInapp());
        }
        if (request.getOptOutPush() != null) {
            udm.setOptOutPush(request.getOptOutPush());
        }

        udm.computeLookupHashes();
        udm = udmRepo.save(udm);
        return mapToProfileResponse(udm);
    }

    /**
     * Map UserDetailMaster entity to UserProfileResponse DTO
     */
    private com.example.authservice.dto.UserProfileResponse mapToProfileResponse(UserDetailMaster udm) {
        com.example.authservice.dto.UserProfileResponse response = new com.example.authservice.dto.UserProfileResponse();
        response.setUserId(udm.getUserId());
        response.setUsername(udm.getUsername());
        response.setEmail(udm.getEmail());
        response.setMobile(udm.getMobile());
        response.setEmployeeId(udm.getEmployeeId());
        response.setFirstName(udm.getFirstName());
        response.setLastName(udm.getLastName());
        response.setPincode(udm.getPincode());
        response.setCity(udm.getCity());
        response.setState(udm.getState());
        response.setCountry(udm.getCountry());
        response.setCountryCode(udm.getCountryCode());
        response.setAddress1(udm.getAddress1());
        response.setAddress2(udm.getAddress2());
        response.setAddress3(udm.getAddress3());
        response.setProfilePhotoUrl(udm.getProfilePhotoUrl());
        response.setLinkedinUrl(udm.getLinkedinUrl());
        response.setFacebookUrl(udm.getFacebookUrl());
        response.setTwitterUrl(udm.getTwitterUrl());
        response.setInstagramUrl(udm.getInstagramUrl());
        response.setGithubUrl(udm.getGithubUrl());
        response.setWebsiteUrl(udm.getWebsiteUrl());
        response.setDateOfBirth(udm.getDateOfBirth());
        response.setGender(udm.getGender());
        response.setOccupation(udm.getOccupation());
        response.setEducation(udm.getEducation());
        response.setMaritalStatus(udm.getMaritalStatus());
        response.setPreferences(udm.getPreferences());
        response.setActivityPatterns(udm.getActivityPatterns());
        response.setInterests(udm.getInterests());
        response.setBio(udm.getBio());
        response.setSkills(udm.getSkills());
        response.setLanguages(udm.getLanguages());
        response.setTimezone(udm.getTimezone());
        response.setAdditionalInfo(udm.getAdditionalInfo());
        response.setLastLoginDate(udm.getLastLoginDate());
        response.setAccountLocked(udm.getAccountLocked());
        response.setAcceptTc(udm.getAcceptTc());
        response.setOptOutSms(udm.getOptOutSms());
        response.setOptOutEmail(udm.getOptOutEmail());
        response.setOptOutWhatsapp(udm.getOptOutWhatsapp());
        response.setOptOutInapp(udm.getOptOutInapp());
        response.setOptOutPush(udm.getOptOutPush());
        return response;
    }

    // =====================================================
    // BLOCK / UNBLOCK / PERMANENT BLOCK (Security, Compliance, PDPA/DPDPA)
    // =====================================================
    private void ensureAdmin(Long currentUserId) {
        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName() != null && r.getName().equalsIgnoreCase("ROLE_ADMIN"));
        if (!isAdmin)
            throw new RuntimeException("Access denied: only admins can perform this action");
    }

    /** When unblocking a permanently blocked user, only admin is allowed. Throws with a specific message if not admin. */
    private void ensureAdminForPermanentUnblock(Long currentUserId) {
        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName() != null && r.getName().equalsIgnoreCase("ROLE_ADMIN"));
        if (!isAdmin)
            throw new RuntimeException("Only an administrator can unblock a permanently blocked account.");
    }

    /** Store actor as userId for PDPA-friendly audit (no PII in block record). */
    private String actorForAudit(Long userId) {
        return "userId:" + userId;
    }

    /**
     * Temporary block: user cannot log in until unblocked. Stores reason and optional blockedUntil for audit.
     */
    @Transactional
    public void blockUser(Long targetUserId, String reason, LocalDateTime blockedUntil, Long currentUserId) {
        ensureAdmin(currentUserId);
        User target = userRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        UserDetailMaster udm = udmRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User detail not found"));

        target.setEnabled(false);
        userRepo.save(target);
        udm.setAccountLocked(true);
        udm.setBlockType("TEMPORARY");
        udm.setBlockReason(reason);
        udm.setBlockedAt(LocalDateTime.now());
        udm.setBlockedBy(actorForAudit(currentUserId));
        udm.setBlockedUntil(blockedUntil);
        udm.computeLookupHashes();
        udmRepo.save(udm);

        auditService.log(currentUserId, "USER_BLOCK", "User", String.valueOf(targetUserId),
                (reason != null ? reason : "") + (blockedUntil != null ? "|until=" + blockedUntil : ""),
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    /**
     * Unblock: restore access. Temporarily blocked users require admin; permanently blocked users can be unblocked only by admin.
     */
    @Transactional
    public void unblockUser(Long targetUserId, Long currentUserId) {
        User target = userRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        UserDetailMaster udm = udmRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User detail not found"));

        if ("PERMANENT".equalsIgnoreCase(udm.getBlockType())) {
            ensureAdminForPermanentUnblock(currentUserId);
        } else {
            ensureAdmin(currentUserId);
        }

        target.setEnabled(true);
        userRepo.save(target);
        udm.setAccountLocked(false);
        udm.setBlockType("NONE");
        udm.setBlockReason(null);
        udm.setBlockedAt(null);
        udm.setBlockedBy(null);
        udm.setBlockedUntil(null);
        udm.computeLookupHashes();
        udmRepo.save(udm);

        auditService.log(currentUserId, "USER_UNBLOCK", "User", String.valueOf(targetUserId), null,
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    /**
     * Permanent block: revoke access indefinitely. Reversal requires separate process (e.g. data protection request).
     */
    @Transactional
    public void permanentBlockUser(Long targetUserId, String reason, Long currentUserId) {
        ensureAdmin(currentUserId);
        User target = userRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        UserDetailMaster udm = udmRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User detail not found"));

        target.setEnabled(false);
        userRepo.save(target);
        udm.setAccountLocked(true);
        udm.setBlockType("PERMANENT");
        udm.setBlockReason(reason);
        udm.setBlockedAt(LocalDateTime.now());
        udm.setBlockedBy(actorForAudit(currentUserId));
        udm.setBlockedUntil(null);
        udm.computeLookupHashes();
        udmRepo.save(udm);

        auditService.log(currentUserId, "USER_PERMANENT_BLOCK", "User", String.valueOf(targetUserId),
                reason != null ? reason : "",
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    /**
     * Soft self-delete: user deletes their own account. Marks account as deleted, revokes all sessions,
     * and prevents future login. Data is retained for audit/compliance.
     */
    @Transactional
    public void softDeleteSelf(Long currentUserId) {
        if (currentUserId == null) throw new RuntimeException("Unauthorized: No active user context");

        User user = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + currentUserId));

        if (user.getDeletedAt() != null)
            throw new RuntimeException("Account has already been deleted");

        // Revoke all sessions and delete refresh tokens so user cannot continue using the app
        authService.revokeAllUserSessions(currentUserId);
        authService.deleteAllTokensForUser(currentUserId);

        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy("userId:" + currentUserId);
        userRepo.save(user);

        auditService.log(currentUserId, "USER_SELF_DELETE", "User", String.valueOf(currentUserId), null,
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

}




