package com.example.helpdesk.service;

import com.example.helpdesk.dto.IssueEscalationRequest;
import com.example.helpdesk.dto.IssueEscalationResponse;
import com.example.helpdesk.entity.EscalationMatrix;
import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.entity.IssueEscalation;
import com.example.helpdesk.enums.SupportLevel;
import com.example.helpdesk.repository.EscalationMatrixRepository;
import com.example.helpdesk.repository.IssueEscalationRepository;
import com.example.helpdesk.repository.IssueRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalationService {
    private final IssueRepository issueRepository;
    private final IssueEscalationRepository escalationRepository;
    private final EscalationMatrixRepository escalationMatrixRepository;
    private final SLAService slaService;

    public EscalationService(
            IssueRepository issueRepository,
            IssueEscalationRepository escalationRepository,
            EscalationMatrixRepository escalationMatrixRepository,
            SLAService slaService) {
        this.issueRepository = issueRepository;
        this.escalationRepository = escalationRepository;
        this.escalationMatrixRepository = escalationMatrixRepository;
        this.slaService = slaService;
    }

    @Transactional
    public IssueEscalationResponse escalateIssue(Long issueId, IssueEscalationRequest request) {
        String username = JwtUtil.getUsernameOrThrow();
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        SupportLevel currentLevel = issue.getCurrentSupportLevel();
        SupportLevel targetLevel = request.getToLevel();

        // Validate escalation
        if (currentLevel != null && !canEscalate(currentLevel, targetLevel)) {
            throw new RuntimeException("Cannot escalate from " + currentLevel + " to " + targetLevel);
        }

        // Create escalation record
        IssueEscalation escalation = new IssueEscalation();
        escalation.setIssue(issue);
        escalation.setFromLevel(currentLevel);
        escalation.setToLevel(targetLevel);
        escalation.setEscalationReason(
                request.getEscalationReason() != null ? request.getEscalationReason() : "Manual escalation");
        escalation.setEscalatedBy(username);
        escalation.setCreatedBy(username);

        IssueEscalation saved = escalationRepository.save(escalation);

        // Update issue
        issue.setCurrentSupportLevel(targetLevel);
        issue.setEscalationCount(issue.getEscalationCount() + 1);
        issue.setLastEscalatedAt(LocalDateTime.now());
        issue.setUpdatedBy(username);
        issueRepository.save(issue);

        // Update SLA tracking
        slaService.updateSLATrackingForEscalation(issue, targetLevel);

        return mapToResponse(saved);
    }

    @Transactional
    public void autoEscalateIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        if (issue.getStatus().name().equals("RESOLVED") || issue.getStatus().name().equals("CLOSED")) {
            return; // Don't escalate resolved/closed issues
        }

        EscalationMatrix matrix = escalationMatrixRepository
                .findByRelatedServiceAndPriorityAndSupportLevel(
                        issue.getRelatedService(),
                        issue.getPriority(),
                        issue.getCurrentSupportLevel())
                .orElse(null);

        if (matrix == null || matrix.getEscalateToLevel() == null) {
            return; // No escalation path defined
        }

        // Check if escalation time has passed
        LocalDateTime lastEscalatedAt = issue.getLastEscalatedAt() != null
                ? issue.getLastEscalatedAt()
                : issue.getAssignedAt() != null ? issue.getAssignedAt() : issue.getCreatedAt();

        if (lastEscalatedAt != null && matrix.getEscalationTimeMinutes() != null) {
            long minutesSinceLastAction = java.time.Duration.between(lastEscalatedAt, LocalDateTime.now()).toMinutes();
            if (minutesSinceLastAction >= matrix.getEscalationTimeMinutes()) {
                // Auto-escalate
                IssueEscalationRequest escalationRequest = new IssueEscalationRequest();
                escalationRequest.setToLevel(matrix.getEscalateToLevel());
                escalationRequest.setEscalationReason("Auto-escalation: SLA time exceeded");

                escalateIssue(issueId, escalationRequest);
            }
        }
    }

    public List<IssueEscalationResponse> getIssueEscalations(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + issueId));

        return escalationRepository.findByIssueOrderByEscalatedAtDesc(issue).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean canEscalate(SupportLevel from, SupportLevel to) {
        // Can only escalate upward: L1 -> L2 -> L3
        if (from == SupportLevel.L1 && (to == SupportLevel.L2 || to == SupportLevel.L3)) {
            return true;
        }
        if (from == SupportLevel.L2 && to == SupportLevel.L3) {
            return true;
        }
        // Can also de-escalate if needed (e.g., L3 -> L2)
        return from == SupportLevel.L3 && to == SupportLevel.L2;
    }

    private IssueEscalationResponse mapToResponse(IssueEscalation escalation) {
        IssueEscalationResponse response = new IssueEscalationResponse();
        response.setId(escalation.getId());
        response.setIssueId(escalation.getIssue().getId());
        response.setFromLevel(escalation.getFromLevel());
        response.setToLevel(escalation.getToLevel());
        response.setEscalatedAt(escalation.getEscalatedAt());
        response.setEscalationReason(escalation.getEscalationReason());
        response.setEscalatedBy(escalation.getEscalatedBy());
        return response;
    }
}

