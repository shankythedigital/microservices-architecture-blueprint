package com.example.asset.repository;

import com.example.asset.entity.PurchaseOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ✅ PurchaseOutletRepository
 * JPA repository for PurchaseOutlet entity.
 * Includes a custom method for unique outlet name validation.
 */
@Repository
public interface PurchaseOutletRepository extends JpaRepository<PurchaseOutlet, Long> {

    /**
     * Checks if an outlet with the given name already exists (case-sensitive).
     * Used for enforcing unique outlet names.
     *
     * @param outletName the outlet name to check
     * @return true if the outlet exists, false otherwise
     */
    boolean existsByOutletName(String outletName);
    
    /**
     * Checks if an outlet with the given name already exists (case-insensitive).
     * Preferred for duplicate detection to prevent "Outlet A" and "outlet a" duplicates.
     *
     * @param outletName the outlet name to check
     * @return true if the outlet exists, false otherwise
     */
    boolean existsByOutletNameIgnoreCase(String outletName);
    
    /**
     * Find outlet by name (case-insensitive)
     */
    Optional<PurchaseOutlet> findByOutletNameIgnoreCase(String outletName);
}

