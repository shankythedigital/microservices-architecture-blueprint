package com.example.helpdesk.repository;

import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByStatus(IssueStatus status);
    List<Issue> findByRelatedService(RelatedService relatedService);
    List<Issue> findByReportedBy(String reportedBy);

    List<Issue> findByReportedByOrderByCreatedAtDesc(String reportedBy);

    List<Issue> findByAssignedTo(String assignedTo);
    List<Issue> findByStatusAndRelatedService(IssueStatus status, RelatedService relatedService);

    /**
     * Rows with {@code login_user_id} set match by id; legacy rows (null) still match encrypted {@code reportedBy}.
     */
    @Query("SELECT i FROM Issue i WHERE i.loginUserId = :loginUserId OR (i.loginUserId IS NULL AND i.reportedBy = :reportedBy) ORDER BY i.createdAt DESC")
    List<Issue> findMineByLoginUserIdOrLegacyReportedBy(@Param("loginUserId") Long loginUserId, @Param("reportedBy") String reportedBy);
}

