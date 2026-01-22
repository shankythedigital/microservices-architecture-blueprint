package com.example.helpdesk.controller;

import com.example.helpdesk.dto.SLATrackingResponse;
import com.example.helpdesk.service.SLAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/sla")
@Tag(name = "SLA Tracking", description = "APIs for SLA tracking and monitoring")
public class SLAController {
    private final SLAService slaService;

    public SLAController(SLAService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/issue/{issueId}")
    @Operation(summary = "Get SLA tracking", description = "Retrieve SLA tracking information for an issue")
    public ResponseEntity<SLATrackingResponse> getSLATracking(@PathVariable Long issueId) {
        SLATrackingResponse tracking = slaService.getSLATracking(issueId);
        return ResponseEntity.ok(tracking);
    }

    @GetMapping("/breaches")
    @Operation(summary = "Get SLA breaches", description = "Retrieve all issues with SLA breaches")
    public ResponseEntity<List<SLATrackingResponse>> getSLABreaches() {
        List<SLATrackingResponse> breaches = slaService.getSLABreaches();
        return ResponseEntity.ok(breaches);
    }

    @PostMapping("/issue/{issueId}/first-response")
    @Operation(summary = "Record first response", description = "Record the first response time for an issue")
    public ResponseEntity<Void> recordFirstResponse(@PathVariable Long issueId) {
        slaService.updateFirstResponse(issueId);
        return ResponseEntity.ok().build();
    }
}

