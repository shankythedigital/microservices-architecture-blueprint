package com.example.asset.repository;

import com.example.asset.entity.ComplianceRuleTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceRuleTypeMasterRepository extends JpaRepository<ComplianceRuleTypeMaster, Long> {
    Optional<ComplianceRuleTypeMaster> findByCodeIgnoreCase(String code);
    List<ComplianceRuleTypeMaster> findAllByActiveTrue();
    List<ComplianceRuleTypeMaster> findAllByCategoryAndActiveTrue(String category);
    boolean existsByCodeIgnoreCase(String code);
}
