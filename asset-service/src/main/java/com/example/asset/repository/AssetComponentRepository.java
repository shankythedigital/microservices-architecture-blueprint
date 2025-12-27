
package com.example.asset.repository;

import com.example.asset.entity.AssetComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * ✅ Repository for managing AssetComponent entities.
 * Provides convenience methods for name uniqueness and soft delete filtering.
 */
@Repository
public interface AssetComponentRepository extends JpaRepository<AssetComponent, Long> {
    /**
     * Check if component exists by name (case-sensitive)
     */
    boolean existsByComponentName(String componentName);
    
    /**
     * Check if component exists by name (case-insensitive) - preferred for duplicate detection
     */
    boolean existsByComponentNameIgnoreCase(String componentName);
    
    /**
     * Find component by name (case-insensitive)
     */
    Optional<AssetComponent> findByComponentNameIgnoreCase(String componentName);
    
    boolean existsById(Long componentId);
    Optional<AssetComponent> findById(Long id);
}



