package com.example.helpdesk.controller;

import com.example.helpdesk.dto.QueryAnswerRequest;
import com.example.helpdesk.dto.QueryRequest;
import com.example.helpdesk.dto.QueryResponse;
import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/queries")
@Tag(name = "Query Management", description = "APIs for managing user queries")
public class QueryController {
    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping
    @Operation(summary = "Create a new query", description = "Submit a new query")
    public ResponseEntity<QueryResponse> createQuery(@Valid @RequestBody QueryRequest request) {
        QueryResponse response = queryService.createQuery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all queries", description = "Retrieve all queries")
    public ResponseEntity<List<QueryResponse>> getAllQueries() {
        List<QueryResponse> queries = queryService.getAllQueries();
        return ResponseEntity.ok(queries);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get query by ID", description = "Retrieve a specific query by its ID")
    public ResponseEntity<QueryResponse> getQueryById(@PathVariable Long id) {
        QueryResponse query = queryService.getQueryById(id);
        return ResponseEntity.ok(query);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get queries by status", description = "Retrieve queries filtered by status")
    public ResponseEntity<List<QueryResponse>> getQueriesByStatus(@PathVariable QueryStatus status) {
        List<QueryResponse> queries = queryService.getQueriesByStatus(status);
        return ResponseEntity.ok(queries);
    }

    @GetMapping("/service/{service}")
    @Operation(summary = "Get queries by service", description = "Retrieve queries filtered by related service")
    public ResponseEntity<List<QueryResponse>> getQueriesByService(@PathVariable RelatedService service) {
        List<QueryResponse> queries = queryService.getQueriesByService(service);
        return ResponseEntity.ok(queries);
    }

    @GetMapping("/my-queries")
    @Operation(summary = "Get my queries", description = "Retrieve queries asked by the current user")
    public ResponseEntity<List<QueryResponse>> getMyQueries() {
        List<QueryResponse> queries = queryService.getMyQueries();
        return ResponseEntity.ok(queries);
    }

    @PostMapping("/{id}/answer")
    @Operation(summary = "Answer a query", description = "Provide an answer to a pending query")
    public ResponseEntity<QueryResponse> answerQuery(
            @PathVariable Long id,
            @Valid @RequestBody QueryAnswerRequest request) {
        QueryResponse query = queryService.answerQuery(id, request);
        return ResponseEntity.ok(query);
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close query", description = "Close a query")
    public ResponseEntity<QueryResponse> closeQuery(@PathVariable Long id) {
        QueryResponse query = queryService.closeQuery(id);
        return ResponseEntity.ok(query);
    }
}

