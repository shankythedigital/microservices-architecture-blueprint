package com.example.asset.service;

import com.example.asset.entity.EntityTypeMaster;
import com.example.asset.enums.EntityType;
import com.example.asset.repository.EntityTypeMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ✅ EntityTypeService
 * Service for managing entity types.
 * Provides methods to retrieve and validate entity types.
 */
@Service
public class EntityTypeService {

    private static final Logger log = LoggerFactory.getLogger(EntityTypeService.class);

    private final EntityTypeMasterRepository repository;

    public EntityTypeService(EntityTypeMasterRepository repository) {
        this.repository = repository;
    }

    // ============================================================
    // 📋 LIST ALL ENTITY TYPES
    // ============================================================
    public List<EntityTypeMaster> listAll() {
        return repository.findAll();
    }

    // ============================================================
    // 📋 LIST ACTIVE ENTITY TYPES
    // ============================================================
    public List<EntityTypeMaster> listActive() {
        return repository.findAllByActiveTrue();
    }

    // ============================================================
    // 🔍 FIND BY CODE
    // ============================================================
    public Optional<EntityTypeMaster> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // ✅ VALIDATE ENTITY TYPE
    // ============================================================
    public boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.existsByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // ✅ VALIDATE ACTIVE ENTITY TYPE
    // ============================================================
    public boolean isValidAndActive(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return repository.findByCodeIgnoreCaseAndActiveTrue(code.trim()).isPresent();
    }

    // ============================================================
    // 🟢 INITIALIZE ENTITY TYPES (Seed Data)
    // ============================================================
    @Transactional
    public void initializeEntityTypes() {
        log.info("🔄 Initializing entity types...");
        
        for (EntityType type : EntityType.values()) {
            Optional<EntityTypeMaster> existing = repository.findByCodeIgnoreCase(type.getCode());
            
            if (existing.isEmpty()) {
                EntityTypeMaster entity = new EntityTypeMaster(type.getCode(), type.getDescription());
                entity.setActive(true);
                entity.setCreatedBy("SYSTEM");
                repository.save(entity);
                log.info("✅ Created entity type: {} - {}", type.getCode(), type.getDescription());
            } else {
                EntityTypeMaster entity = existing.get();
                // Update description if it changed
                if (!type.getDescription().equals(entity.getDescription())) {
                    entity.setDescription(type.getDescription());
                    entity.setUpdatedBy("SYSTEM");
                    repository.save(entity);
                    log.info("✏️ Updated entity type: {} - {}", type.getCode(), type.getDescription());
                }
            }
        }
        
        log.info("✅ Entity types initialization completed");
    }

    // ============================================================
    // 🔍 FIND BY ID
    // ============================================================
    public Optional<EntityTypeMaster> findById(Integer id) {
        return repository.findById(id);
    }
}
