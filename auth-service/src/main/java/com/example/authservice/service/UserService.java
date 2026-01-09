
package com.example.authservice.service;

import com.example.authservice.dto.UserDto;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.model.User;
import com.example.authservice.model.UserDetailMaster;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserDetailMasterRepository;
import com.example.authservice.repository.UserRepository;
import com.example.common.util.HmacUtil;
import com.example.common.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepo;
    @Autowired private UserDetailMasterRepository udmRepo;
    @Autowired private RefreshTokenRepository refreshRepo;
    @Autowired private UserMapper userMapper;
    @Autowired private FileStorageUtil fileStorageUtil;

    public UserDto getMyProfile(Long currentUserId) {
        if (currentUserId == null) throw new RuntimeException("Unauthorized: No active user context");

        User user = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + currentUserId));

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
            UserDetailMaster udm = udmRepo.findByUserId(u.getUserId()).orElse(null);
            out.add(userMapper.toDto(u, udm));
        }
        return out;
    }

    public Long resolveUserId(String identifier, String type) {
        if (identifier == null || identifier.isBlank()) return null;
        String hash = HmacUtil.hmacHex(identifier);
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
        String hash = HmacUtil.hmacHex(identifier);

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

        UserDetailMaster udm = udmRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + userId));

        return mapToProfileResponse(udm);
    }

    /**
     * Update user profile (extended profile fields)
     * Users can only update their own profile unless they are admin
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

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !userId.equals(currentUserId)) {
            throw new RuntimeException("Access denied: not authorized to update another user's profile");
        }

        UserDetailMaster udm = udmRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found for userId: " + userId));

        // Handle profile photo update - delete old photo if new one is provided
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
        response.setPincode(udm.getPincode());
        response.setCity(udm.getCity());
        response.setState(udm.getState());
        response.setCountry(udm.getCountry());
        response.setCountryCode(udm.getCountryCode());
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
        return response;
    }

}




