package com.example.helpdesk.repository;

import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByStatus(IssueStatus status);
    List<Issue> findByRelatedService(RelatedService relatedService);
    List<Issue> findByReportedBy(String reportedBy);
    List<Issue> findByAssignedTo(String assignedTo);
    List<Issue> findByStatusAndRelatedService(IssueStatus status, RelatedService relatedService);
}

