package com.example.asset.service;

import com.example.asset.entity.StatusMaster;
import com.example.asset.enums.Status;
import com.example.asset.repository.StatusMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ✅ StatusService
 * Service for managing status values.
 * Provides methods to retrieve and validate statuses.
 */
@Service
public class StatusService {

    private static final Logger log = LoggerFactory.getLogger(StatusService.class);

    private final StatusMasterRepository repository;

    public StatusService(StatusMasterRepository repository) {
        this.repository = repository;
    }

    // ============================================================
    // 📋 LIST ALL STATUSES
    // ============================================================
    public List<StatusMaster> listAll() {
        return repository.findAll();
    }

    // ============================================================
    // 📋 LIST ACTIVE STATUSES
    // ============================================================
    public List<StatusMaster> listActive() {
        return repository.findAllByActiveTrue();
    }

    // ============================================================
    // 📋 LIST STATUSES BY CATEGORY
    // ============================================================
    public List<StatusMaster> listByCategory(String category) {
        if (category == null || category.isBlank()) {
            return List.of();
        }
        return repository.findAllByCategoryIgnoreCase(category.trim());
    }

    // ============================================================
    // 📋 LIST ACTIVE STATUSES BY CATEGORY
    // ============================================================
    public List<StatusMaster> listActiveByCategory(String category) {
        if (category == null || category.isBlank()) {
            return List.of();
        }
        return repository.findAllByCategoryIgnoreCaseAndActiveTrue(category.trim());
    }

    // ============================================================
    // 🔍 FIND BY CODE
    // ============================================================
    public Optional<StatusMaster> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // ✅ VALIDATE STATUS
    // ============================================================
    public boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.existsByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // ✅ VALIDATE ACTIVE STATUS
    // ============================================================
    public boolean isValidAndActive(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.findByCodeIgnoreCaseAndActiveTrue(code.trim()).isPresent();
    }

    // ============================================================
    // 🟢 INITIALIZE STATUSES (Seed Data)
    // ============================================================
    @Transactional
    public void initializeStatuses() {
        log.info("🔄 Initializing status values...");
        
        for (Status status : Status.values()) {
            Optional<StatusMaster> existing = repository.findByCodeIgnoreCase(status.getCode());
            
            if (existing.isEmpty()) {
                StatusMaster entity = new StatusMaster(
                        status.getCode(), 
                        status.getDescription(), 
                        status.getCategory());
                entity.setActive(true);
                entity.setCreatedBy("SYSTEM");
                repository.save(entity);
                log.info("✅ Created status: {} - {} [{}]", 
                        status.getCode(), status.getDescription(), status.getCategory());
            } else {
                StatusMaster entity = existing.get();
                // Update description and category if they changed
                boolean updated = false;
                if (!status.getDescription().equals(entity.getDescription())) {
                    entity.setDescription(status.getDescription());
                    updated = true;
                }
                if (!status.getCategory().equals(entity.getCategory())) {
                    entity.setCategory(status.getCategory());
                    updated = true;
                }
                if (updated) {
                    entity.setUpdatedBy("SYSTEM");
                    repository.save(entity);
                    log.info("✏️ Updated status: {} - {} [{}]", 
                            status.getCode(), status.getDescription(), status.getCategory());
                }
            }
        }
        
        log.info("✅ Status values initialization completed");
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    public Optional<StatusMaster> findById(Integer id) {
        return repository.findById(id);
    }

    // ============================================================
    // ⭐ FAVOURITE / MOST LIKE / SEQUENCE ORDER OPERATIONS
    // ============================================================
    
    /**
     * Toggle favourite status for a status (accessible to all authenticated users)
     */
    @Transactional
    public StatusMaster updateFavourite(HttpHeaders headers, Integer id, Boolean isFavourite) {
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        return repository.findById(id).map(existing -> {
            existing.setIsFavourite(isFavourite != null ? isFavourite : false);
            existing.setUpdatedBy(username);
            StatusMaster saved = repository.save(existing);
            log.info("⭐ Status favourite updated: id={} isFavourite={} by={}", id, isFavourite, username);
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Status not found with id: " + id));
    }

    /**
     * Toggle most like status for a status (accessible to all authenticated users)
     */
    @Transactional
    public StatusMaster updateMostLike(HttpHeaders headers, Integer id, Boolean isMostLike) {
        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        return repository.findById(id).map(existing -> {
            existing.setIsMostLike(isMostLike != null ? isMostLike : false);
            existing.setUpdatedBy(username);
            StatusMaster saved = repository.save(existing);
            log.info("⭐ Status most like updated: id={} isMostLike={} by={}", id, isMostLike, username);
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Status not found with id: " + id));
    }

    /**
     * Update sequence order for a status (admin only)
     */
    @Transactional
    public StatusMaster updateSequenceOrder(HttpHeaders headers, Integer id, Integer sequenceOrder) {
        // Check if user is admin
        if (!com.example.asset.util.JwtUtil.isAdmin()) {
            throw new RuntimeException("Access denied: Only admins can update sequence order");
        }

        String username = com.example.asset.util.JwtUtil.getUsernameOrThrow();
        return repository.findById(id).map(existing -> {
            existing.setSequenceOrder(sequenceOrder);
            existing.setUpdatedBy(username);
            StatusMaster saved = repository.save(existing);
            log.info("📊 Status sequence order updated: id={} sequenceOrder={} by={}", id, sequenceOrder, username);
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Status not found with id: " + id));
    }
}
