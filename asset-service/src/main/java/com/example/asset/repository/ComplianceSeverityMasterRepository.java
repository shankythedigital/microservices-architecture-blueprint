package com.example.asset.repository;

import com.example.asset.entity.ComplianceSeverityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceSeverityMasterRepository extends JpaRepository<ComplianceSeverityMaster, Long> {
    Optional<ComplianceSeverityMaster> findByCodeIgnoreCase(String code);
    List<ComplianceSeverityMaster> findAllByActiveTrue();
    List<ComplianceSeverityMaster> findAllByActiveTrueOrderByLevelAsc();
    boolean existsByCodeIgnoreCase(String code);
}
