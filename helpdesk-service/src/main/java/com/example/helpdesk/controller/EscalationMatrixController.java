package com.example.helpdesk.controller;

import com.example.helpdesk.dto.EscalationMatrixRequest;
import com.example.helpdesk.dto.EscalationMatrixResponse;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.service.EscalationMatrixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/escalation-matrix")
@Tag(name = "Escalation Matrix", description = "APIs for managing escalation matrix and SLA configuration")
public class EscalationMatrixController {
    private final EscalationMatrixService escalationMatrixService;

    public EscalationMatrixController(EscalationMatrixService escalationMatrixService) {
        this.escalationMatrixService = escalationMatrixService;
    }

    @PostMapping
    @Operation(summary = "Create escalation matrix", description = "Create a new escalation matrix entry with SLA configuration")
    public ResponseEntity<EscalationMatrixResponse> createEscalationMatrix(@Valid @RequestBody EscalationMatrixRequest request) {
        EscalationMatrixResponse response = escalationMatrixService.createEscalationMatrix(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all escalation matrices", description = "Retrieve all escalation matrix configurations")
    public ResponseEntity<List<EscalationMatrixResponse>> getAllEscalationMatrices() {
        List<EscalationMatrixResponse> matrices = escalationMatrixService.getAllEscalationMatrices();
        return ResponseEntity.ok(matrices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get escalation matrix by ID", description = "Retrieve a specific escalation matrix by its ID")
    public ResponseEntity<EscalationMatrixResponse> getEscalationMatrixById(@PathVariable Long id) {
        EscalationMatrixResponse matrix = escalationMatrixService.getEscalationMatrixById(id);
        return ResponseEntity.ok(matrix);
    }

    @GetMapping("/service/{service}")
    @Operation(summary = "Get escalation matrices by service", description = "Retrieve escalation matrices for a specific service")
    public ResponseEntity<List<EscalationMatrixResponse>> getEscalationMatricesByService(@PathVariable RelatedService service) {
        List<EscalationMatrixResponse> matrices = escalationMatrixService.getEscalationMatricesByService(service);
        return ResponseEntity.ok(matrices);
    }

    @GetMapping("/service/{service}/priority/{priority}")
    @Operation(summary = "Get escalation matrix", description = "Get active escalation matrix for service and priority")
    public ResponseEntity<EscalationMatrixResponse> getEscalationMatrix(
            @PathVariable RelatedService service,
            @PathVariable IssuePriority priority) {
        EscalationMatrixResponse matrix = escalationMatrixService.getEscalationMatrix(service, priority);
        return ResponseEntity.ok(matrix);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update escalation matrix", description = "Update an existing escalation matrix")
    public ResponseEntity<EscalationMatrixResponse> updateEscalationMatrix(
            @PathVariable Long id,
            @Valid @RequestBody EscalationMatrixRequest request) {
        EscalationMatrixResponse matrix = escalationMatrixService.updateEscalationMatrix(id, request);
        return ResponseEntity.ok(matrix);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete escalation matrix", description = "Delete an escalation matrix")
    public ResponseEntity<Void> deleteEscalationMatrix(@PathVariable Long id) {
        escalationMatrixService.deleteEscalationMatrix(id);
        return ResponseEntity.noContent().build();
    }
}

