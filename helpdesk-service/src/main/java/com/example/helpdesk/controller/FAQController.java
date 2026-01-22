package com.example.helpdesk.controller;

import com.example.helpdesk.dto.FAQRequest;
import com.example.helpdesk.dto.FAQResponse;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.service.FAQService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/faqs")
@Tag(name = "FAQ Management", description = "APIs for managing frequently asked questions")
public class FAQController {
    private final FAQService faqService;

    public FAQController(FAQService faqService) {
        this.faqService = faqService;
    }

    @PostMapping
    @Operation(summary = "Create a new FAQ", description = "Add a new frequently asked question")
    public ResponseEntity<FAQResponse> createFAQ(@Valid @RequestBody FAQRequest request) {
        FAQResponse response = faqService.createFAQ(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all FAQs", description = "Retrieve all FAQs")
    public ResponseEntity<List<FAQResponse>> getAllFAQs() {
        List<FAQResponse> faqs = faqService.getAllFAQs();
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get FAQ by ID", description = "Retrieve a specific FAQ by its ID")
    public ResponseEntity<FAQResponse> getFAQById(@PathVariable Long id) {
        FAQResponse faq = faqService.getFAQById(id);
        return ResponseEntity.ok(faq);
    }

    @GetMapping("/service/{service}")
    @Operation(summary = "Get FAQs by service", description = "Retrieve FAQs filtered by related service")
    public ResponseEntity<List<FAQResponse>> getFAQsByService(@PathVariable RelatedService service) {
        List<FAQResponse> faqs = faqService.getFAQsByService(service);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get FAQs by category", description = "Retrieve FAQs filtered by category")
    public ResponseEntity<List<FAQResponse>> getFAQsByCategory(@PathVariable String category) {
        List<FAQResponse> faqs = faqService.getFAQsByCategory(category);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/search")
    @Operation(summary = "Search FAQs", description = "Search FAQs by keyword")
    public ResponseEntity<List<FAQResponse>> searchFAQs(@RequestParam String keyword) {
        List<FAQResponse> faqs = faqService.searchFAQs(keyword);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/service/{service}/search")
    @Operation(summary = "Search FAQs by service", description = "Search FAQs by service and keyword")
    public ResponseEntity<List<FAQResponse>> searchFAQsByService(
            @PathVariable RelatedService service,
            @RequestParam String keyword) {
        List<FAQResponse> faqs = faqService.searchFAQsByService(service, keyword);
        return ResponseEntity.ok(faqs);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update FAQ", description = "Update an existing FAQ")
    public ResponseEntity<FAQResponse> updateFAQ(
            @PathVariable Long id,
            @Valid @RequestBody FAQRequest request) {
        FAQResponse faq = faqService.updateFAQ(id, request);
        return ResponseEntity.ok(faq);
    }

    @PostMapping("/{id}/helpful")
    @Operation(summary = "Mark FAQ as helpful", description = "Increment the helpful count for an FAQ")
    public ResponseEntity<Void> markFAQAsHelpful(@PathVariable Long id) {
        faqService.markFAQAsHelpful(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete FAQ", description = "Delete an FAQ")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }
}

