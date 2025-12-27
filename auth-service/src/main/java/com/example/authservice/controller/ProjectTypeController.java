package com.example.authservice.controller;

import com.example.authservice.model.ProjectType;
import com.example.authservice.service.ProjectTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ ProjectTypeController
 * REST endpoints for managing project types.
 */
@RestController
@RequestMapping("/api/auth/v1/project-types")
public class ProjectTypeController {

    private static final Logger log = LoggerFactory.getLogger(ProjectTypeController.class);
    private final ProjectTypeService projectTypeService;

    public ProjectTypeController(ProjectTypeService projectTypeService) {
        this.projectTypeService = projectTypeService;
    }

    // ============================================================
    // 📋 CREATE PROJECT TYPE
    // ============================================================
    @PostMapping
    public ResponseEntity<?> createProjectType(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "displayOrder", defaultValue = "0") Integer displayOrder,
            @RequestParam(value = "createdBy", defaultValue = "SYSTEM") String createdBy) {
        try {
            ProjectType projectType = projectTypeService.createProjectType(
                    code, name, description, displayOrder, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(projectType);
        } catch (IllegalArgumentException e) {
            log.error("❌ Failed to create project type: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error creating project type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to create project type");
        }
    }

    // ============================================================
    // 📋 UPDATE PROJECT TYPE
    // ============================================================
    @PutMapping("/{projectTypeId}")
    public ResponseEntity<?> updateProjectType(
            @PathVariable Long projectTypeId,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "updatedBy", defaultValue = "SYSTEM") String updatedBy) {
        try {
            ProjectType projectType = projectTypeService.updateProjectType(
                    projectTypeId, name, description, displayOrder, updatedBy);
            return ResponseEntity.ok(projectType);
        } catch (IllegalArgumentException e) {
            log.error("❌ Failed to update project type: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error updating project type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to update project type");
        }
    }

    // ============================================================
    // 📋 DELETE PROJECT TYPE
    // ============================================================
    @DeleteMapping("/{projectTypeId}")
    public ResponseEntity<?> deleteProjectType(
            @PathVariable Long projectTypeId,
            @RequestParam(value = "deletedBy", defaultValue = "SYSTEM") String deletedBy) {
        try {
            projectTypeService.deleteProjectType(projectTypeId, deletedBy);
            return ResponseEntity.ok("✅ Project type deleted successfully");
        } catch (IllegalArgumentException e) {
            log.error("❌ Failed to delete project type: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error deleting project type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to delete project type");
        }
    }

    // ============================================================
    // 📋 GET PROJECT TYPE BY CODE
    // ============================================================
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getProjectTypeByCode(@PathVariable String code) {
        try {
            Optional<ProjectType> projectType = projectTypeService.findByCode(code);
            if (projectType.isPresent()) {
                return ResponseEntity.ok(projectType.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Failed to get project type by code: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to get project type");
        }
    }

    // ============================================================
    // 📋 GET PROJECT TYPE BY ID
    // ============================================================
    @GetMapping("/{projectTypeId}")
    public ResponseEntity<?> getProjectTypeById(@PathVariable Long projectTypeId) {
        try {
            Optional<ProjectType> projectType = projectTypeService.findById(projectTypeId);
            if (projectType.isPresent()) {
                return ResponseEntity.ok(projectType.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Failed to get project type by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to get project type");
        }
    }

    // ============================================================
    // 📋 GET ALL ACTIVE PROJECT TYPES
    // ============================================================
    @GetMapping
    public ResponseEntity<?> getAllActiveProjectTypes() {
        try {
            List<ProjectType> projectTypes = projectTypeService.getAllActive();
            return ResponseEntity.ok(projectTypes);
        } catch (Exception e) {
            log.error("❌ Failed to get project types: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to get project types");
        }
    }

    // ============================================================
    // 📋 VALIDATE PROJECT TYPE
    // ============================================================
    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateProjectType(@PathVariable String code) {
        try {
            boolean exists = projectTypeService.validateProjectTypeExists(code);
            Map<String, Object> response = new HashMap<>();
            response.put("code", code);
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to validate project type: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to validate project type");
        }
    }
}

