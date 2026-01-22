package com.example.helpdesk.controller;

import com.example.helpdesk.dto.IssueEscalationRequest;
import com.example.helpdesk.dto.IssueEscalationResponse;
import com.example.helpdesk.service.EscalationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/escalations")
@Tag(name = "Issue Escalation", description = "APIs for managing issue escalations")
public class EscalationController {
    private final EscalationService escalationService;

    public EscalationController(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @PostMapping("/issue/{issueId}")
    @Operation(summary = "Escalate issue", description = "Manually escalate an issue to a higher support level")
    public ResponseEntity<IssueEscalationResponse> escalateIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody IssueEscalationRequest request) {
        IssueEscalationResponse response = escalationService.escalateIssue(issueId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/issue/{issueId}/auto-escalate")
    @Operation(summary = "Auto-escalate issue", description = "Trigger auto-escalation check for an issue")
    public ResponseEntity<Void> autoEscalateIssue(@PathVariable Long issueId) {
        escalationService.autoEscalateIssue(issueId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/issue/{issueId}")
    @Operation(summary = "Get issue escalations", description = "Retrieve escalation history for an issue")
    public ResponseEntity<List<IssueEscalationResponse>> getIssueEscalations(@PathVariable Long issueId) {
        List<IssueEscalationResponse> escalations = escalationService.getIssueEscalations(issueId);
        return ResponseEntity.ok(escalations);
    }
}

