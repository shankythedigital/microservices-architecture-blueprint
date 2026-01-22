package com.example.asset.controller;

import com.example.asset.dto.AssetScanCreateRequest;
import com.example.asset.dto.AssetScanRequest;
import com.example.asset.dto.AssetScanResponse;
import com.example.asset.service.AssetScanService;
import com.example.asset.util.JwtUtil;
import com.example.common.security.JwtVerifier;
import com.example.common.util.ResponseWrapper;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * ✅ AssetScanController
 * Universal REST controller for scanning QR codes and barcodes to identify assets.
 * Supports scanning by Asset ID, Asset Name UDV, or Serial Number.
 * All scan operations are logged in the audit log.
 */
@RestController
@RequestMapping("/api/asset/v1/scan")
@Tag(name = "Asset Scanning", description = "Universal QR Code and Barcode Scanning API")
public class AssetScanController {

    private static final Logger log = LoggerFactory.getLogger(AssetScanController.class);

    private final AssetScanService scanService;
    private final JwtVerifier jwtVerifier;

    public AssetScanController(AssetScanService scanService, JwtVerifier jwtVerifier) {
        this.scanService = scanService;
        this.jwtVerifier = jwtVerifier;
    }

    // ============================================================
    // 📱 SCAN ASSET BY QR CODE / BARCODE (POST)
    // ============================================================
    @PostMapping
    @Operation(
        summary = "Scan asset by QR code or barcode",
        description = "Universal endpoint to scan any type of asset using QR code or barcode. " +
                     "Supports matching by Asset ID (numeric), Asset Name UDV, or Serial Number. " +
                     "All scan operations are automatically logged in the audit log."
    )
    public ResponseEntity<ResponseWrapper<AssetScanResponse>> scanAsset(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetScanRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("📱 [POST] /scan - Scanning asset with value: '{}'", request.getScanValue());
            
            // Extract username from JWT token
            String username = extractUsernameFromToken(headers);
            
            // Validate request
            if (request == null || !org.springframework.util.StringUtils.hasText(request.getScanValue())) {
                log.warn("⚠️ Invalid scan request: empty scan value");
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Scan value cannot be empty", null));
            }
            
            // Perform scan
            Optional<AssetScanResponse> result = scanService.scanAsset(
                    request.getScanValue(),
                    request.getScanType(),
                    username,
                    httpRequest
            );
            
            if (result.isPresent()) {
                log.info("✅ Asset scan successful: Asset ID={}, Matched by={}", 
                        result.get().getAssetId(), result.get().getMatchedBy());
                return ResponseEntity.ok(
                        new ResponseWrapper<>(
                                true,
                                String.format("✅ Asset found (Matched by: %s)", result.get().getMatchedBy()),
                                result.get()
                        )
                );
            } else {
                log.warn("⚠️ Asset scan failed: No asset found for value '{}'", request.getScanValue());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseWrapper<>(
                                false,
                                String.format("❌ No asset found for scan value: '%s'", request.getScanValue()),
                                null
                        ));
            }
            
        } catch (Exception e) {
            log.error("❌ Asset scan failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📱 SCAN ASSET BY QR CODE / BARCODE (GET - Convenience)
    // ============================================================
    @GetMapping
    @Operation(
        summary = "Scan asset by QR code or barcode (GET)",
        description = "Convenience GET endpoint for scanning. Pass scan value as query parameter. " +
                     "Supports matching by Asset ID (numeric), Asset Name UDV, or Serial Number. " +
                     "All scan operations are automatically logged in the audit log."
    )
    public ResponseEntity<ResponseWrapper<AssetScanResponse>> scanAssetGet(
            @RequestHeader HttpHeaders headers,
            @RequestParam("value") String scanValue,
            @RequestParam(value = "type", required = false, defaultValue = "AUTO") String scanType,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("📱 [GET] /scan - Scanning asset with value: '{}'", scanValue);
            
            // Extract username from JWT token
            String username = extractUsernameFromToken(headers);
            
            // Validate request
            if (!org.springframework.util.StringUtils.hasText(scanValue)) {
                log.warn("⚠️ Invalid scan request: empty scan value");
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Scan value cannot be empty", null));
            }
            
            // Perform scan
            Optional<AssetScanResponse> result = scanService.scanAsset(
                    scanValue,
                    scanType,
                    username,
                    httpRequest
            );
            
            if (result.isPresent()) {
                log.info("✅ Asset scan successful: Asset ID={}, Matched by={}", 
                        result.get().getAssetId(), result.get().getMatchedBy());
                return ResponseEntity.ok(
                        new ResponseWrapper<>(
                                true,
                                String.format("✅ Asset found (Matched by: %s)", result.get().getMatchedBy()),
                                result.get()
                        )
                );
            } else {
                log.warn("⚠️ Asset scan failed: No asset found for value '{}'", scanValue);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseWrapper<>(
                                false,
                                String.format("❌ No asset found for scan value: '%s'", scanValue),
                                null
                        ));
            }
            
        } catch (Exception e) {
            log.error("❌ Asset scan failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 📱 SCAN AND SAVE ASSET (with AI Agent)
    // ============================================================
    @PostMapping("/save")
    @Operation(
        summary = "Scan QR/barcode and create/update asset with all related entities",
        description = "Universal endpoint that scans QR/barcode, uses AI agent to extract structured data, " +
                     "and intelligently creates/updates assets with warranty, AMC, user links, and components. " +
                     "All operations are logged in audit log. The AI agent automatically extracts data from " +
                     "JSON QR codes or pattern-based text."
    )
    public ResponseEntity<ResponseWrapper<AssetScanResponse>> scanAndSave(
            @RequestHeader HttpHeaders headers,
            @RequestBody AssetScanCreateRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("📱 [POST] /scan/save - Scanning and saving asset with value: '{}'", request.getScanValue());
            
            // Extract username and userId from JWT token if not provided
            String username = extractUsernameFromToken(headers);
            if (request.getUsername() == null) {
                request.setUsername(username);
            }
            
            // Extract userId from token if not provided
            try {
                String authHeader = headers.getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    Claims claims = jwtVerifier.parseToken(token).getBody();
                    String userIdStr = claims.getSubject();
                    if (userIdStr != null && request.getUserId() == null) {
                        try {
                            request.setUserId(Long.parseLong(userIdStr));
                        } catch (NumberFormatException e) {
                            log.warn("⚠️ Could not parse userId from token: {}", userIdStr);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Could not extract userId from token: {}", e.getMessage());
            }
            
            // Validate request
            if (!org.springframework.util.StringUtils.hasText(request.getScanValue())) {
                log.warn("⚠️ Invalid scan request: empty scan value");
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "❌ Scan value cannot be empty", null));
            }
            
            // Perform scan and save
            AssetScanResponse result = scanService.scanAndSave(headers, request, httpRequest);
            
            log.info("✅ Asset scan and save successful: Asset ID={}, Matched by={}", 
                    result.getAssetId(), result.getMatchedBy());
            return ResponseEntity.ok(
                    new ResponseWrapper<>(
                            true,
                            String.format("✅ Asset scanned and saved successfully (Asset ID: %d)", result.getAssetId()),
                            result
                    )
            );
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ Asset scan and save failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseWrapper<>(false, "❌ " + e.getMessage(), null));
        }
    }

    // ============================================================
    // 🔐 EXTRACT USERNAME FROM JWT TOKEN
    // ============================================================
    private String extractUsernameFromToken(HttpHeaders headers) {
        try {
            // Try using JwtUtil first (from SecurityContext)
            Optional<String> usernameOpt = JwtUtil.getUsername();
            if (usernameOpt.isPresent()) {
                return usernameOpt.get();
            }
            
            // Fallback: Extract from Authorization header
            String authHeader = headers.getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Claims claims = jwtVerifier.parseToken(token).getBody();
                
                Object usernameObj = claims.get("username");
                if (usernameObj == null) {
                    usernameObj = claims.get("preferred_username");
                }
                if (usernameObj != null) {
                    return usernameObj.toString();
                }
                
                // Last resort: use subject (userId)
                String subject = claims.getSubject();
                if (subject != null) {
                    return subject;
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not extract username from token: {}", e.getMessage());
        }
        
        return "SYSTEM"; // Default fallback
    }
}

