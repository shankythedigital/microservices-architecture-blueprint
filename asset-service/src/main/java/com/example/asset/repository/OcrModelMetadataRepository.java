package com.example.asset.repository;

import com.example.asset.entity.OcrModelMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrModelMetadataRepository extends JpaRepository<OcrModelMetadata, Long> {
    
    Optional<OcrModelMetadata> findByIsActiveTrue();
    
    Optional<OcrModelMetadata> findByModelVersion(String modelVersion);
    
    List<OcrModelMetadata> findByModelTypeOrderByTrainedAtDesc(String modelType);
    
    List<OcrModelMetadata> findAllByOrderByTrainedAtDesc();
}

