package com.example.helpdesk.controller;

import com.example.helpdesk.dto.IssueMasterRequest;
import com.example.helpdesk.dto.IssueMasterResponse;
import com.example.helpdesk.service.IssueMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Issue Master: predefined issue types linked to category, subcategory, component, or spare part.
 * When issue is not in list, user can add by selecting from master tables.
 * Raise ticket = select from this list.
 */
@RestController
@RequestMapping("/api/helpdesk/issue-master")
@Tag(name = "Issue Master", description = "Predefined issue types linked to category/subcategory/component/spare part")
public class IssueMasterController {
    private final IssueMasterService issueMasterService;

    public IssueMasterController(IssueMasterService issueMasterService) {
        this.issueMasterService = issueMasterService;
    }

    @PostMapping
    @Operation(summary = "Create issue master", description = "Add new issue type when not in list. Select category, subcategory, component, or spare part from master tables.")
    public ResponseEntity<IssueMasterResponse> create(@Valid @RequestBody IssueMasterRequest request) {
        IssueMasterResponse response = issueMasterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all issue masters", description = "Get all predefined issue types")
    public ResponseEntity<List<IssueMasterResponse>> listAll() {
        return ResponseEntity.ok(issueMasterService.listAll());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List by category", description = "Get issue masters linked to a category")
    public ResponseEntity<List<IssueMasterResponse>> listByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(issueMasterService.listByCategory(categoryId));
    }

    @GetMapping("/sub-category/{subCategoryId}")
    @Operation(summary = "List by subcategory", description = "Get issue masters linked to a subcategory")
    public ResponseEntity<List<IssueMasterResponse>> listBySubCategory(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(issueMasterService.listBySubCategory(subCategoryId));
    }

    @GetMapping("/component/{componentId}")
    @Operation(summary = "List by component", description = "Get issue masters linked to a component")
    public ResponseEntity<List<IssueMasterResponse>> listByComponent(@PathVariable Long componentId) {
        return ResponseEntity.ok(issueMasterService.listByComponent(componentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue master by ID", description = "Retrieve a specific issue master")
    public ResponseEntity<IssueMasterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(issueMasterService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update issue master", description = "Update an issue master entry")
    public ResponseEntity<IssueMasterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IssueMasterRequest request) {
        return ResponseEntity.ok(issueMasterService.update(id, request));
    }
}
