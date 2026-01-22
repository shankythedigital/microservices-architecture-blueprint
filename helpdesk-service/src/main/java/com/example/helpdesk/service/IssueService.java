package com.example.helpdesk.service;

import com.example.helpdesk.dto.IssueRequest;
import com.example.helpdesk.dto.IssueResponse;
import com.example.helpdesk.dto.IssueResolutionRequest;
import com.example.helpdesk.dto.SLATrackingResponse;
import com.example.helpdesk.entity.Issue;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.SupportLevel;
import com.example.helpdesk.repository.IssueRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final EscalationMatrixService escalationMatrixService;
    private final SLAService slaService;

    public IssueService(
            IssueRepository issueRepository,
            EscalationMatrixService escalationMatrixService,
            SLAService slaService) {
        this.issueRepository = issueRepository;
        this.escalationMatrixService = escalationMatrixService;
        this.slaService = slaService;
    }

    @Transactional
    public IssueResponse createIssue(IssueRequest request) {
        String userId = JwtUtil.getUserIdOrThrow();
        String username = JwtUtil.getUsernameOrThrow();

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setPriority(request.getPriority());
        issue.setRelatedService(request.getRelatedService());
        issue.setStatus(IssueStatus.OPEN);
        issue.setReportedBy(username);
        issue.setCreatedBy(username);

        // Assign initial support level based on escalation matrix
        SupportLevel initialLevel = escalationMatrixService.getInitialSupportLevel(
                request.getRelatedService(), request.getPriority());
        issue.setInitialSupportLevel(initialLevel);
        issue.setCurrentSupportLevel(initialLevel);

        Issue saved = issueRepository.save(issue);

        // Create SLA tracking
        slaService.createSLATracking(saved);

        return mapToResponse(saved);
    }

    public List<IssueResponse> getAllIssues() {
        return issueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IssueResponse getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));
        return mapToResponse(issue);
    }

    public List<IssueResponse> getIssuesByStatus(IssueStatus status) {
        return issueRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueResponse> getIssuesByService(com.example.helpdesk.enums.RelatedService service) {
        return issueRepository.findByRelatedService(service).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueResponse> getMyIssues() {
        String username = JwtUtil.getUsernameOrThrow();
        return issueRepository.findByReportedBy(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse updateIssueStatus(Long id, IssueStatus status) {
        String username = JwtUtil.getUsernameOrThrow();
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));
        issue.setStatus(status);
        issue.setUpdatedBy(username);
        Issue saved = issueRepository.save(issue);

        // Update first response if status changed to IN_PROGRESS
        if (status == IssueStatus.IN_PROGRESS && issue.getFirstResponseAt() == null) {
            slaService.updateFirstResponse(id);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public IssueResponse assignIssue(Long id, String assignedTo) {
        String username = JwtUtil.getUsernameOrThrow();
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));
        issue.setAssignedTo(assignedTo);
        issue.setStatus(IssueStatus.IN_PROGRESS);
        if (issue.getAssignedAt() == null) {
            issue.setAssignedAt(LocalDateTime.now());
        }
        issue.setUpdatedBy(username);
        Issue saved = issueRepository.save(issue);
        return mapToResponse(saved);
    }

    @Transactional
    public IssueResponse resolveIssue(Long id, IssueResolutionRequest request) {
        String username = JwtUtil.getUsernameOrThrow();
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));
        issue.setResolution(request.getResolution());
        issue.setStatus(IssueStatus.RESOLVED);
        issue.setUpdatedBy(username);
        Issue saved = issueRepository.save(issue);

        // Update SLA tracking for resolution
        slaService.updateResolution(id);

        return mapToResponse(saved);
    }

    @Transactional
    public IssueResponse closeIssue(Long id) {
        String username = JwtUtil.getUsernameOrThrow();
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found with id: " + id));
        issue.setStatus(IssueStatus.CLOSED);
        issue.setUpdatedBy(username);
        Issue saved = issueRepository.save(issue);
        return mapToResponse(saved);
    }

    private IssueResponse mapToResponse(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.setId(issue.getId());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setRelatedService(issue.getRelatedService());
        response.setReportedBy(issue.getReportedBy());
        response.setAssignedTo(issue.getAssignedTo());
        response.setCurrentSupportLevel(issue.getCurrentSupportLevel());
        response.setInitialSupportLevel(issue.getInitialSupportLevel());
        response.setAssignedAt(issue.getAssignedAt());
        response.setFirstResponseAt(issue.getFirstResponseAt());
        response.setResolution(issue.getResolution());
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());
        response.setResolvedAt(issue.getResolvedAt());
        response.setEscalationCount(issue.getEscalationCount());
        response.setLastEscalatedAt(issue.getLastEscalatedAt());

        // Include SLA tracking if available
        if (issue.getSlaTracking() != null) {
            SLATrackingResponse slaResponse = new SLATrackingResponse();
            slaResponse.setId(issue.getSlaTracking().getId());
            slaResponse.setIssueId(issue.getId());
            slaResponse.setResponseTimeMinutes(issue.getSlaTracking().getResponseTimeMinutes());
            slaResponse.setResolutionTimeMinutes(issue.getSlaTracking().getResolutionTimeMinutes());
            slaResponse.setFirstResponseAt(issue.getSlaTracking().getFirstResponseAt());
            slaResponse.setResolvedAt(issue.getSlaTracking().getResolvedAt());
            slaResponse.setResponseSLAMet(issue.getSlaTracking().getResponseSLAMet());
            slaResponse.setResolutionSLAMet(issue.getSlaTracking().getResolutionSLAMet());
            slaResponse.setResponseSLABreachAt(issue.getSlaTracking().getResponseSLABreachAt());
            slaResponse.setResolutionSLABreachAt(issue.getSlaTracking().getResolutionSLABreachAt());
            slaResponse.setActualResponseTimeMinutes(issue.getSlaTracking().getActualResponseTimeMinutes());
            slaResponse.setActualResolutionTimeMinutes(issue.getSlaTracking().getActualResolutionTimeMinutes());
            response.setSlaTracking(slaResponse);
        }

        return response;
    }
}

