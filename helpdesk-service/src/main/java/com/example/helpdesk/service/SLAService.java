package com.example.helpdesk.service;

import com.example.helpdesk.dto.SLATrackingResponse;
import com.example.helpdesk.entity.EscalationMatrix;
import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.entity.SLATracking;
import com.example.helpdesk.enums.SupportLevel;
import com.example.helpdesk.repository.EscalationMatrixRepository;
import com.example.helpdesk.repository.IssueRepository;
import com.example.helpdesk.repository.SLATrackingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SLAService {
    private final SLATrackingRepository slaTrackingRepository;
    private final IssueRepository issueRepository;
    private final EscalationMatrixRepository escalationMatrixRepository;

    public SLAService(
            SLATrackingRepository slaTrackingRepository,
            IssueRepository issueRepository,
            EscalationMatrixRepository escalationMatrixRepository) {
        this.slaTrackingRepository = slaTrackingRepository;
        this.issueRepository = issueRepository;
        this.escalationMatrixRepository = escalationMatrixRepository;
    }

    @Transactional
    public SLATracking createSLATracking(Issue issue) {
        EscalationMatrix matrix = escalationMatrixRepository
                .findByRelatedServiceAndPriorityAndSupportLevel(
                        issue.getRelatedService(),
                        issue.getPriority(),
                        issue.getCurrentSupportLevel())
                .orElse(null);

        SLATracking tracking = new SLATracking();
        tracking.setIssue(issue);

        if (matrix != null) {
            tracking.setResponseTimeMinutes(matrix.getResponseTimeMinutes());
            tracking.setResolutionTimeMinutes(matrix.getResolutionTimeMinutes());
        } else {
            // Default SLA values
            tracking.setResponseTimeMinutes(60); // 1 hour default
            tracking.setResolutionTimeMinutes(480); // 8 hours default
        }

        tracking.setResponseSLAMet(null);
        tracking.setResolutionSLAMet(null);

        return slaTrackingRepository.save(tracking);
    }

    @Transactional
    public void updateFirstResponse(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        SLATracking tracking = slaTrackingRepository.findByIssue(issue)
                .orElseGet(() -> createSLATracking(issue));

        if (tracking.getFirstResponseAt() == null) {
            tracking.setFirstResponseAt(LocalDateTime.now());
            issue.setFirstResponseAt(LocalDateTime.now());

            // Calculate actual response time
            long minutes = java.time.Duration.between(issue.getCreatedAt(), LocalDateTime.now()).toMinutes();
            tracking.setActualResponseTimeMinutes((int) minutes);

            // Check if response SLA is met
            if (tracking.getResponseTimeMinutes() != null) {
                boolean slaMet = minutes <= tracking.getResponseTimeMinutes();
                tracking.setResponseSLAMet(slaMet);

                if (!slaMet) {
                    tracking.setResponseSLABreachAt(LocalDateTime.now());
                }
            }

            slaTrackingRepository.save(tracking);
            issueRepository.save(issue);
        }
    }

    @Transactional
    public void updateResolution(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        SLATracking tracking = slaTrackingRepository.findByIssue(issue)
                .orElseGet(() -> createSLATracking(issue));

        if (tracking.getResolvedAt() == null && issue.getResolvedAt() != null) {
            tracking.setResolvedAt(issue.getResolvedAt());

            // Calculate actual resolution time
            long minutes = java.time.Duration.between(issue.getCreatedAt(), issue.getResolvedAt()).toMinutes();
            tracking.setActualResolutionTimeMinutes((int) minutes);

            // Check if resolution SLA is met
            if (tracking.getResolutionTimeMinutes() != null) {
                boolean slaMet = minutes <= tracking.getResolutionTimeMinutes();
                tracking.setResolutionSLAMet(slaMet);

                if (!slaMet) {
                    tracking.setResolutionSLABreachAt(LocalDateTime.now());
                }
            }

            slaTrackingRepository.save(tracking);
        }
    }

    @Transactional
    public void updateSLATrackingForEscalation(Issue issue, SupportLevel newLevel) {
        EscalationMatrix matrix = escalationMatrixRepository
                .findByRelatedServiceAndPriorityAndSupportLevel(
                        issue.getRelatedService(),
                        issue.getPriority(),
                        newLevel)
                .orElse(null);

        SLATracking tracking = slaTrackingRepository.findByIssue(issue)
                .orElseGet(() -> createSLATracking(issue));

        if (matrix != null) {
            // Update SLA targets based on new support level
            tracking.setResponseTimeMinutes(matrix.getResponseTimeMinutes());
            tracking.setResolutionTimeMinutes(matrix.getResolutionTimeMinutes());
            slaTrackingRepository.save(tracking);
        }
    }

    public SLATrackingResponse getSLATracking(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        SLATracking tracking = slaTrackingRepository.findByIssue(issue)
                .orElseGet(() -> createSLATracking(issue));

        return mapToResponse(tracking);
    }

    public List<SLATrackingResponse> getSLABreaches() {
        return slaTrackingRepository.findSLABreaches().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void checkSLAViolations() {
        List<Issue> openIssues = issueRepository.findAll().stream()
                .filter(i -> !i.getStatus().name().equals("RESOLVED") && !i.getStatus().name().equals("CLOSED"))
                .toList();

        for (Issue issue : openIssues) {
            SLATracking tracking = slaTrackingRepository.findByIssue(issue)
                    .orElseGet(() -> createSLATracking(issue));

            LocalDateTime now = LocalDateTime.now();

            // Check response SLA
            if (tracking.getFirstResponseAt() == null && tracking.getResponseTimeMinutes() != null) {
                long minutesSinceCreation = java.time.Duration.between(issue.getCreatedAt(), now).toMinutes();
                if (minutesSinceCreation > tracking.getResponseTimeMinutes() && tracking.getResponseSLABreachAt() == null) {
                    tracking.setResponseSLABreachAt(now);
                    tracking.setResponseSLAMet(false);
                    slaTrackingRepository.save(tracking);
                }
            }

            // Check resolution SLA
            if (issue.getResolvedAt() == null && tracking.getResolutionTimeMinutes() != null) {
                long minutesSinceCreation = java.time.Duration.between(issue.getCreatedAt(), now).toMinutes();
                if (minutesSinceCreation > tracking.getResolutionTimeMinutes() && tracking.getResolutionSLABreachAt() == null) {
                    tracking.setResolutionSLABreachAt(now);
                    tracking.setResolutionSLAMet(false);
                    slaTrackingRepository.save(tracking);
                }
            }
        }
    }

    private SLATrackingResponse mapToResponse(SLATracking tracking) {
        SLATrackingResponse response = new SLATrackingResponse();
        response.setId(tracking.getId());
        response.setIssueId(tracking.getIssue().getId());
        response.setResponseTimeMinutes(tracking.getResponseTimeMinutes());
        response.setResolutionTimeMinutes(tracking.getResolutionTimeMinutes());
        response.setFirstResponseAt(tracking.getFirstResponseAt());
        response.setResolvedAt(tracking.getResolvedAt());
        response.setResponseSLAMet(tracking.getResponseSLAMet());
        response.setResolutionSLAMet(tracking.getResolutionSLAMet());
        response.setResponseSLABreachAt(tracking.getResponseSLABreachAt());
        response.setResolutionSLABreachAt(tracking.getResolutionSLABreachAt());
        response.setActualResponseTimeMinutes(tracking.getActualResponseTimeMinutes());
        response.setActualResolutionTimeMinutes(tracking.getActualResolutionTimeMinutes());
        return response;
    }
}

