package com.example.helpdesk.repository;

import com.example.helpdesk.entity.EscalationMatrix;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscalationMatrixRepository extends JpaRepository<EscalationMatrix, Long> {
    Optional<EscalationMatrix> findByRelatedServiceAndPriorityAndSupportLevel(
            RelatedService relatedService, IssuePriority priority, SupportLevel supportLevel);
    
    List<EscalationMatrix> findByRelatedServiceAndPriority(RelatedService relatedService, IssuePriority priority);
    
    List<EscalationMatrix> findByRelatedService(RelatedService relatedService);
    
    List<EscalationMatrix> findByIsActiveTrue();
    
    Optional<EscalationMatrix> findByRelatedServiceAndPriorityAndIsActiveTrue(
            RelatedService relatedService, IssuePriority priority);
}

