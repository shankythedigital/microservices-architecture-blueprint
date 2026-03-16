package com.example.helpdesk.service;

import com.example.helpdesk.dto.IssueMasterRequest;
import com.example.helpdesk.dto.IssueMasterResponse;
import com.example.helpdesk.entity.IssueMaster;
import com.example.helpdesk.repository.IssueMasterRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IssueMasterService {
    private final IssueMasterRepository issueMasterRepository;

    public IssueMasterService(IssueMasterRepository issueMasterRepository) {
        this.issueMasterRepository = issueMasterRepository;
    }

    @Transactional
    public IssueMasterResponse create(IssueMasterRequest request) {
        validateAtLeastOneRef(request);
        String username = JwtUtil.getUsernameOrThrow();

        IssueMaster master = new IssueMaster();
        master.setIssueTitle(request.getIssueTitle());
        master.setIssueDescription(request.getIssueDescription());
        master.setCategoryId(request.getCategoryId());
        master.setSubCategoryId(request.getSubCategoryId());
        master.setComponentId(request.getComponentId());
        master.setSparePartId(request.getSparePartId());
        master.setCreatedBy(username);

        IssueMaster saved = issueMasterRepository.save(master);
        return mapToResponse(saved);
    }

    public List<IssueMasterResponse> listAll() {
        return issueMasterRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueMasterResponse> listByCategory(Long categoryId) {
        return issueMasterRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueMasterResponse> listBySubCategory(Long subCategoryId) {
        return issueMasterRepository.findBySubCategoryId(subCategoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueMasterResponse> listByComponent(Long componentId) {
        return issueMasterRepository.findByComponentId(componentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IssueMasterResponse getById(Long id) {
        IssueMaster master = issueMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue master not found with id: " + id));
        return mapToResponse(master);
    }

    public IssueMaster findEntityById(Long id) {
        return issueMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue master not found with id: " + id));
    }

    @Transactional
    public IssueMasterResponse update(Long id, IssueMasterRequest request) {
        validateAtLeastOneRef(request);
        String username = JwtUtil.getUsernameOrThrow();
        IssueMaster master = issueMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue master not found with id: " + id));

        master.setIssueTitle(request.getIssueTitle());
        master.setIssueDescription(request.getIssueDescription());
        master.setCategoryId(request.getCategoryId());
        master.setSubCategoryId(request.getSubCategoryId());
        master.setComponentId(request.getComponentId());
        master.setSparePartId(request.getSparePartId());
        master.setUpdatedBy(username);

        IssueMaster saved = issueMasterRepository.save(master);
        return mapToResponse(saved);
    }

    private void validateAtLeastOneRef(IssueMasterRequest request) {
        boolean hasRef = request.getCategoryId() != null || request.getSubCategoryId() != null
                || request.getComponentId() != null || request.getSparePartId() != null;
        if (!hasRef) {
            throw new IllegalArgumentException("At least one of categoryId, subCategoryId, componentId, or sparePartId must be provided");
        }
    }

    private IssueMasterResponse mapToResponse(IssueMaster master) {
        IssueMasterResponse r = new IssueMasterResponse();
        r.setId(master.getId());
        r.setIssueTitle(master.getIssueTitle());
        r.setIssueDescription(master.getIssueDescription());
        r.setCategoryId(master.getCategoryId());
        r.setSubCategoryId(master.getSubCategoryId());
        r.setComponentId(master.getComponentId());
        r.setSparePartId(master.getSparePartId());
        r.setCreatedAt(master.getCreatedAt());
        r.setUpdatedAt(master.getUpdatedAt());
        return r;
    }
}
