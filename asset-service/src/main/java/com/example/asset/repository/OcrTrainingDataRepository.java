package com.example.asset.repository;

import com.example.asset.entity.OcrTrainingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrTrainingDataRepository extends JpaRepository<OcrTrainingData, Long> {
    
    Optional<OcrTrainingData> findByImageHash(String imageHash);
    
    List<OcrTrainingData> findByUserIdAndIsCorrectedTrue(Long userId);
    
    List<OcrTrainingData> findBySubCategoryId(Long subCategoryId);
    
    List<OcrTrainingData> findByMakeId(Long makeId);
    
    List<OcrTrainingData> findByIsCorrectedTrue();
    
    long countByIsCorrectedTrue();
    
    long countByIsCorrectedFalse();
}

