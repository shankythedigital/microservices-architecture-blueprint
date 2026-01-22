package com.example.helpdesk.repository;

import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.entity.IssueEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueEscalationRepository extends JpaRepository<IssueEscalation, Long> {
    List<IssueEscalation> findByIssue(Issue issue);
    
    List<IssueEscalation> findByIssueOrderByEscalatedAtDesc(Issue issue);
}

