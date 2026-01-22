package com.example.helpdesk.controller;

import com.example.helpdesk.dto.ServiceKnowledgeRequest;
import com.example.helpdesk.dto.ServiceKnowledgeResponse;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.service.ServiceKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/knowledge")
@Tag(name = "Service Knowledge", description = "APIs for managing service knowledge base")
public class ServiceKnowledgeController {
    private final ServiceKnowledgeService knowledgeService;

    public ServiceKnowledgeController(ServiceKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping
    @Operation(summary = "Create knowledge entry", description = "Add new knowledge about a service")
    public ResponseEntity<ServiceKnowledgeResponse> createKnowledge(@Valid @RequestBody ServiceKnowledgeRequest request) {
        ServiceKnowledgeResponse response = knowledgeService.createKnowledge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all knowledge", description = "Retrieve all knowledge entries")
    public ResponseEntity<List<ServiceKnowledgeResponse>> getAllKnowledge() {
        List<ServiceKnowledgeResponse> knowledge = knowledgeService.getAllKnowledge();
        return ResponseEntity.ok(knowledge);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get knowledge by ID", description = "Retrieve a specific knowledge entry by its ID")
    public ResponseEntity<ServiceKnowledgeResponse> getKnowledgeById(@PathVariable Long id) {
        ServiceKnowledgeResponse knowledge = knowledgeService.getKnowledgeById(id);
        return ResponseEntity.ok(knowledge);
    }

    @GetMapping("/service/{service}")
    @Operation(summary = "Get knowledge by service", description = "Retrieve knowledge entries filtered by service")
    public ResponseEntity<List<ServiceKnowledgeResponse>> getKnowledgeByService(@PathVariable RelatedService service) {
        List<ServiceKnowledgeResponse> knowledge = knowledgeService.getKnowledgeByService(service);
        return ResponseEntity.ok(knowledge);
    }

    @GetMapping("/service/{service}/search")
    @Operation(summary = "Search knowledge", description = "Search knowledge entries by service and keyword")
    public ResponseEntity<List<ServiceKnowledgeResponse>> searchKnowledge(
            @PathVariable RelatedService service,
            @RequestParam String keyword) {
        List<ServiceKnowledgeResponse> knowledge = knowledgeService.searchKnowledge(service, keyword);
        return ResponseEntity.ok(knowledge);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update knowledge", description = "Update an existing knowledge entry")
    public ResponseEntity<ServiceKnowledgeResponse> updateKnowledge(
            @PathVariable Long id,
            @Valid @RequestBody ServiceKnowledgeRequest request) {
        ServiceKnowledgeResponse knowledge = knowledgeService.updateKnowledge(id, request);
        return ResponseEntity.ok(knowledge);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete knowledge", description = "Delete a knowledge entry")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return ResponseEntity.noContent().build();
    }
}

