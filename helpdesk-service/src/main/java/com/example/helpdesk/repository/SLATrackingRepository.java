package com.example.helpdesk.repository;

import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.entity.SLATracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SLATrackingRepository extends JpaRepository<SLATracking, Long> {
    Optional<SLATracking> findByIssue(Issue issue);
    
    @Query("SELECT s FROM SLATracking s WHERE s.responseSLAMet = false OR s.resolutionSLAMet = false")
    List<SLATracking> findSLABreaches();
    
    @Query("SELECT s FROM SLATracking s WHERE s.issue.relatedService = :service")
    List<SLATracking> findByService(@Param("service") com.example.helpdesk.enums.RelatedService service);
}

