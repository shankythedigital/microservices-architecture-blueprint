package com.example.asset.repository;

import com.example.asset.entity.OcrLearnedPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrLearnedPatternRepository extends JpaRepository<OcrLearnedPattern, Long> {
    
    List<OcrLearnedPattern> findByPatternTypeAndIsActiveTrue(String patternType);
    
    List<OcrLearnedPattern> findByPatternTypeAndSubCategoryIdAndIsActiveTrue(String patternType, Long subCategoryId);
    
    List<OcrLearnedPattern> findByPatternTypeAndMakeIdAndIsActiveTrue(String patternType, Long makeId);
    
    List<OcrLearnedPattern> findByIsActiveTrueOrderByConfidenceWeightDesc();
    
    @Query("SELECT p FROM OcrLearnedPattern p WHERE p.patternType = :patternType AND p.patternRegex = :patternRegex AND p.subCategoryId = :subCategoryId")
    Optional<OcrLearnedPattern> findByPatternTypeAndPatternRegexAndSubCategoryId(
            @Param("patternType") String patternType, 
            @Param("patternRegex") String patternRegex, 
            @Param("subCategoryId") Long subCategoryId);
}

