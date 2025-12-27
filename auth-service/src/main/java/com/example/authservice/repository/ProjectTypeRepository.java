package com.example.authservice.repository;

import com.example.authservice.model.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * ✅ ProjectTypeRepository
 * Repository for ProjectType entity.
 */
@Repository
public interface ProjectTypeRepository extends JpaRepository<ProjectType, Long> {

    /**
     * Find project type by code (case-insensitive)
     */
    Optional<ProjectType> findByCodeIgnoreCase(String code);

    /**
     * Check if project type exists by code
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find all active project types
     */
    List<ProjectType> findByActiveTrue();

    /**
     * Find all active project types ordered by display order
     */
    List<ProjectType> findByActiveTrueOrderByDisplayOrderAsc();
}
