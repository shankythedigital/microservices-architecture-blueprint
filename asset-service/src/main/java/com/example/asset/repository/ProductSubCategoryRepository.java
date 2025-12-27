
package com.example.asset.repository;

import com.example.asset.entity.ProductSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ✅ ProductSubCategoryRepository
 * Provides data access methods for ProductSubCategory entity.
 * Supports standard CRUD and custom finder methods.
 */
@Repository
public interface ProductSubCategoryRepository extends JpaRepository<ProductSubCategory, Long> {

    /**
     * ✅ Check if a subcategory already exists by name (for uniqueness validation).
     * @param subCategoryName Subcategory name to check
     * @return true if a subcategory with the given name already exists
     */
    boolean existsBySubCategoryName(String subCategoryName);
    
    /**
     * ✅ Check if a subcategory already exists by name (case-insensitive).
     * Preferred for duplicate detection to prevent "Laptops" and "laptops" duplicates.
     * @param subCategoryName Subcategory name to check
     * @return true if a subcategory with the given name already exists
     */
    boolean existsBySubCategoryNameIgnoreCase(String subCategoryName);

    /**
     * ✅ Fetch all subcategories that are marked active.
     * @return List of active ProductSubCategory entities
     */
    List<ProductSubCategory> findByActiveTrue();

    /**
     * ✅ Fetch all subcategories by category ID (if needed for dropdowns / filtering).
     * @param categoryId category foreign key
     * @return list of subcategories under that category
     */
    List<ProductSubCategory> findByCategory_CategoryId(Long categoryId);

    /**
     * ✅ Find subcategory by name (case-insensitive)
     * @param subCategoryName Subcategory name to find
     * @return Optional ProductSubCategory
     */
    Optional<ProductSubCategory> findBySubCategoryNameIgnoreCase(String subCategoryName);

    /**
     * ✅ Check if subcategory exists by name and category (case-insensitive)
     * @param subCategoryName Subcategory name
     * @param categoryId Category ID
     * @return true if exists
     */
    boolean existsBySubCategoryNameIgnoreCaseAndCategory_CategoryId(String subCategoryName, Long categoryId);
}

