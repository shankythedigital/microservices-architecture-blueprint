package com.example.authservice.service;

import com.example.authservice.model.ProjectType;
import com.example.authservice.repository.ProjectTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * ✅ ProjectTypeService
 * Service for managing project types.
 */
@Service
public class ProjectTypeService {

    private static final Logger log = LoggerFactory.getLogger(ProjectTypeService.class);
    private final ProjectTypeRepository projectTypeRepo;

    public ProjectTypeService(ProjectTypeRepository projectTypeRepo) {
        this.projectTypeRepo = projectTypeRepo;
    }

    // ============================================================
    // 📋 CREATE PROJECT TYPE
    // ============================================================
    @Transactional
    public ProjectType createProjectType(String code, String name, String description, Integer displayOrder, String createdBy) {
        // Edge case: Null/empty code
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("❌ Project type code cannot be null or empty");
        }

        // Edge case: Null/empty name
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("❌ Project type name cannot be null or empty");
        }

        // Edge case: Duplicate code
        if (projectTypeRepo.existsByCodeIgnoreCase(code.trim())) {
            throw new IllegalArgumentException("❌ Project type with code '" + code + "' already exists");
        }

        ProjectType projectType = new ProjectType();
        projectType.setCode(code.trim().toUpperCase());
        projectType.setName(name.trim());
        projectType.setDescription(description != null ? description.trim() : null);
        projectType.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        projectType.setCreatedBy(createdBy != null ? createdBy : "SYSTEM");
        projectType.setActive(true);

        ProjectType saved = projectTypeRepo.save(projectType);
        log.info("✅ ProjectType created: {} - {}", saved.getCode(), saved.getName());
        return saved;
    }

    // ============================================================
    // 📋 UPDATE PROJECT TYPE
    // ============================================================
    @Transactional
    public ProjectType updateProjectType(Long projectTypeId, String name, String description, 
                                        Integer displayOrder, String updatedBy) {
        // Edge case: Project type not found
        ProjectType projectType = projectTypeRepo.findById(projectTypeId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Project type not found: " + projectTypeId));

        // Edge case: Null/empty name
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("❌ Project type name cannot be null or empty");
        }

        projectType.setName(name.trim());
        if (description != null) {
            projectType.setDescription(description.trim());
        }
        if (displayOrder != null) {
            projectType.setDisplayOrder(displayOrder);
        }
        projectType.setUpdatedBy(updatedBy != null ? updatedBy : "SYSTEM");

        ProjectType saved = projectTypeRepo.save(projectType);
        log.info("✅ ProjectType updated: {} - {}", saved.getCode(), saved.getName());
        return saved;
    }

    // ============================================================
    // 📋 DELETE PROJECT TYPE (Soft Delete)
    // ============================================================
    @Transactional
    public void deleteProjectType(Long projectTypeId, String deletedBy) {
        // Edge case: Project type not found
        ProjectType projectType = projectTypeRepo.findById(projectTypeId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Project type not found: " + projectTypeId));

        // Soft delete
        projectType.setActive(false);
        projectType.setUpdatedBy(deletedBy != null ? deletedBy : "SYSTEM");
        projectTypeRepo.save(projectType);

        log.info("✅ ProjectType deleted: {} - {}", projectType.getCode(), projectType.getName());
    }

    // ============================================================
    // 📋 GET PROJECT TYPE BY CODE
    // ============================================================
    public Optional<ProjectType> findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        return projectTypeRepo.findByCodeIgnoreCase(code.trim());
    }

    // ============================================================
    // 📋 GET PROJECT TYPE BY ID
    // ============================================================
    public Optional<ProjectType> findById(Long projectTypeId) {
        return projectTypeRepo.findById(projectTypeId);
    }

    // ============================================================
    // 📋 GET ALL ACTIVE PROJECT TYPES
    // ============================================================
    public List<ProjectType> getAllActive() {
        return projectTypeRepo.findByActiveTrueOrderByDisplayOrderAsc();
    }

    // ============================================================
    // 📋 VALIDATE PROJECT TYPE EXISTS
    // ============================================================
    public boolean validateProjectTypeExists(String code) {
        if (!StringUtils.hasText(code)) {
            return false;
        }
        return projectTypeRepo.existsByCodeIgnoreCase(code.trim());
    }
}
