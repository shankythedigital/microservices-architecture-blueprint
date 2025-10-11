package com.example.asset.repository;
import com.example.asset.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
