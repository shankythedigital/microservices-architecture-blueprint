package com.example.authservice.controller;

import com.example.authservice.dto.DecryptRequest;
import com.example.authservice.dto.DecryptResponse;
import com.example.authservice.service.DecryptionService;
import com.example.authservice.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-only API to decrypt AES-GCM encrypted values.
 * Uses same key/format as JpaAttributeEncryptor (PII fields: username, email, mobile, etc.).
 * For audit, support, and compliance use cases.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDecryptController {

    @Autowired
    private DecryptionService decryptionService;

    /**
     * Decrypt an encrypted value.
     * Requires ROLE_ADMIN. Input must be Base64-encoded AES-GCM ciphertext.
     *
     * @param request { "encryptedValue": "..." }
     * @return { "decryptedValue": "..." } or 400 with error message if decryption fails
     */
    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(@Valid @RequestBody DecryptRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            DecryptResponse response = decryptionService.decrypt(request.getEncryptedValue());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Decryption failed: " + e.getMessage()));
        }
    }
}
