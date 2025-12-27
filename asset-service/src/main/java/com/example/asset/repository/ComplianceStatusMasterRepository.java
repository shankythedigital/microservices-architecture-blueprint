package com.example.asset.repository;

import com.example.asset.entity.ComplianceStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceStatusMasterRepository extends JpaRepository<ComplianceStatusMaster, Long> {
    Optional<ComplianceStatusMaster> findByCodeIgnoreCase(String code);
    List<ComplianceStatusMaster> findAllByActiveTrue();
    Optional<ComplianceStatusMaster> findByCodeIgnoreCaseAndActiveTrue(String code);
    boolean existsByCodeIgnoreCase(String code);
}
