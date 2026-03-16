
package com.example.authservice.controller;

import com.example.authservice.dto.DecryptFieldResponse;
import com.example.authservice.dto.UserDto;
import com.example.authservice.dto.UserProfileResponse;
import com.example.authservice.service.UserService;
import com.example.authservice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    // GET /api/users/me  -> returns current user's profile (masked PII)
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserDto dto = userService.getMyProfile(currentUserId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Get current user's profile with full decrypted (unmasked) PII.
     * For logged-in users to view their own details.
     */
    @GetMapping("/me/details-decrypted")
    public ResponseEntity<UserProfileResponse> getMyDetailsDecrypted() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserProfileResponse profile = userService.getUserProfileExtended(currentUserId, currentUserId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Decrypt a single field. Call once per field to decrypt values one by one.
     * Allowed fields: username, email, mobile, firstName, lastName, employeeId, pincode, city, state,
     * country, address1, address2, address3, countryCode, dateOfBirth, gender, occupation, education,
     * maritalStatus, profilePhotoUrl, linkedinUrl, facebookUrl, twitterUrl, instagramUrl, githubUrl,
     * websiteUrl, preferences, activityPatterns, interests, bio, skills, languages, timezone, additionalInfo.
     */
    @GetMapping("/me/decrypt/{field}")
    public ResponseEntity<?> decryptMyField(@PathVariable String field) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        try {
            DecryptFieldResponse response = userService.getDecryptedField(currentUserId, field, currentUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Soft self-delete: user deletes their own account. Account is marked as deleted and cannot log in again.
     * Data is retained for audit/compliance.
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        try {
            userService.softDeleteSelf(currentUserId);
            return ResponseEntity.ok(java.util.Map.of("message", "Account deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // GET /api/users/{id} -> admin or self
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserDto dto = userService.getUserProfile(id, currentUserId);
        return ResponseEntity.ok(dto);
    }
}

