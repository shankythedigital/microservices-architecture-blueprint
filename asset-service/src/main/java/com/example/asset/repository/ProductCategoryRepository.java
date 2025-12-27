
package com.example.asset.repository;

import com.example.asset.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByCategoryName(String categoryName);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
    Optional<ProductCategory> findByCategoryNameIgnoreCase(String categoryName);
}

