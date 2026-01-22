package com.example.helpdesk.controller;

import com.example.helpdesk.dto.IssueRequest;
import com.example.helpdesk.dto.IssueResponse;
import com.example.helpdesk.dto.IssueResolutionRequest;
import com.example.helpdesk.enums.IssueStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/issues")
@Tag(name = "Issue Management", description = "APIs for managing helpdesk issues")
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @Operation(summary = "Create a new issue", description = "Raise a new issue ticket")
    public ResponseEntity<IssueResponse> createIssue(@Valid @RequestBody IssueRequest request) {
        IssueResponse response = issueService.createIssue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all issues", description = "Retrieve all issues")
    public ResponseEntity<List<IssueResponse>> getAllIssues() {
        List<IssueResponse> issues = issueService.getAllIssues();
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID", description = "Retrieve a specific issue by its ID")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable Long id) {
        IssueResponse issue = issueService.getIssueById(id);
        return ResponseEntity.ok(issue);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get issues by status", description = "Retrieve issues filtered by status")
    public ResponseEntity<List<IssueResponse>> getIssuesByStatus(@PathVariable IssueStatus status) {
        List<IssueResponse> issues = issueService.getIssuesByStatus(status);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/service/{service}")
    @Operation(summary = "Get issues by service", description = "Retrieve issues filtered by related service")
    public ResponseEntity<List<IssueResponse>> getIssuesByService(@PathVariable RelatedService service) {
        List<IssueResponse> issues = issueService.getIssuesByService(service);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/my-issues")
    @Operation(summary = "Get my issues", description = "Retrieve issues reported by the current user")
    public ResponseEntity<List<IssueResponse>> getMyIssues() {
        List<IssueResponse> issues = issueService.getMyIssues();
        return ResponseEntity.ok(issues);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update issue status", description = "Update the status of an issue")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            @PathVariable Long id,
            @RequestParam IssueStatus status) {
        IssueResponse issue = issueService.updateIssueStatus(id, status);
        return ResponseEntity.ok(issue);
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign issue", description = "Assign an issue to a support agent")
    public ResponseEntity<IssueResponse> assignIssue(
            @PathVariable Long id,
            @RequestParam String assignedTo) {
        IssueResponse issue = issueService.assignIssue(id, assignedTo);
        return ResponseEntity.ok(issue);
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve issue", description = "Resolve an issue with a resolution description")
    public ResponseEntity<IssueResponse> resolveIssue(
            @PathVariable Long id,
            @Valid @RequestBody IssueResolutionRequest request) {
        IssueResponse issue = issueService.resolveIssue(id, request);
        return ResponseEntity.ok(issue);
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close issue", description = "Close an issue")
    public ResponseEntity<IssueResponse> closeIssue(@PathVariable Long id) {
        IssueResponse issue = issueService.closeIssue(id);
        return ResponseEntity.ok(issue);
    }
}

