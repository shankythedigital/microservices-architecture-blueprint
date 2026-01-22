package com.example.helpdesk.service;

import com.example.helpdesk.dto.EscalationMatrixRequest;
import com.example.helpdesk.dto.EscalationMatrixResponse;
import com.example.helpdesk.entity.EscalationMatrix;
import com.example.helpdesk.enums.IssuePriority;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.enums.SupportLevel;
import com.example.helpdesk.repository.EscalationMatrixRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscalationMatrixService {
    private final EscalationMatrixRepository escalationMatrixRepository;

    public EscalationMatrixService(EscalationMatrixRepository escalationMatrixRepository) {
        this.escalationMatrixRepository = escalationMatrixRepository;
    }

    @Transactional
    public EscalationMatrixResponse createEscalationMatrix(EscalationMatrixRequest request) {
        String username = JwtUtil.getUsernameOrThrow();

        EscalationMatrix matrix = new EscalationMatrix();
        matrix.setRelatedService(request.getRelatedService());
        matrix.setPriority(request.getPriority());
        matrix.setSupportLevel(request.getSupportLevel());
        matrix.setInitialAssignmentLevel(request.getInitialAssignmentLevel());
        matrix.setEscalateToLevel(request.getEscalateToLevel());
        matrix.setEscalationTimeMinutes(request.getEscalationTimeMinutes());
        matrix.setResponseTimeMinutes(request.getResponseTimeMinutes());
        matrix.setResolutionTimeMinutes(request.getResolutionTimeMinutes());
        matrix.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        matrix.setCreatedBy(username);

        EscalationMatrix saved = escalationMatrixRepository.save(matrix);
        return mapToResponse(saved);
    }

    public List<EscalationMatrixResponse> getAllEscalationMatrices() {
        return escalationMatrixRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EscalationMatrixResponse getEscalationMatrixById(Long id) {
        EscalationMatrix matrix = escalationMatrixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalation matrix not found with id: " + id));
        return mapToResponse(matrix);
    }

    public List<EscalationMatrixResponse> getEscalationMatricesByService(RelatedService service) {
        return escalationMatrixRepository.findByRelatedService(service).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EscalationMatrixResponse getEscalationMatrix(RelatedService service, IssuePriority priority) {
        EscalationMatrix matrix = escalationMatrixRepository
                .findByRelatedServiceAndPriorityAndIsActiveTrue(service, priority)
                .orElseThrow(() -> new RuntimeException(
                        "No active escalation matrix found for service: " + service + " and priority: " + priority));
        return mapToResponse(matrix);
    }

    @Transactional
    public EscalationMatrixResponse updateEscalationMatrix(Long id, EscalationMatrixRequest request) {
        String username = JwtUtil.getUsernameOrThrow();
        EscalationMatrix matrix = escalationMatrixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalation matrix not found with id: " + id));

        matrix.setRelatedService(request.getRelatedService());
        matrix.setPriority(request.getPriority());
        matrix.setSupportLevel(request.getSupportLevel());
        matrix.setInitialAssignmentLevel(request.getInitialAssignmentLevel());
        matrix.setEscalateToLevel(request.getEscalateToLevel());
        matrix.setEscalationTimeMinutes(request.getEscalationTimeMinutes());
        matrix.setResponseTimeMinutes(request.getResponseTimeMinutes());
        matrix.setResolutionTimeMinutes(request.getResolutionTimeMinutes());
        matrix.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        matrix.setUpdatedBy(username);

        EscalationMatrix saved = escalationMatrixRepository.save(matrix);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteEscalationMatrix(Long id) {
        if (!escalationMatrixRepository.existsById(id)) {
            throw new RuntimeException("Escalation matrix not found with id: " + id);
        }
        escalationMatrixRepository.deleteById(id);
    }

    public SupportLevel getInitialSupportLevel(RelatedService service, IssuePriority priority) {
        EscalationMatrix matrix = escalationMatrixRepository
                .findByRelatedServiceAndPriorityAndIsActiveTrue(service, priority)
                .orElse(null);

        if (matrix != null) {
            return matrix.getInitialAssignmentLevel();
        }

        // Default assignment based on priority
        return switch (priority) {
            case CRITICAL, HIGH -> SupportLevel.L2;
            case MEDIUM -> SupportLevel.L1;
            case LOW -> SupportLevel.L1;
        };
    }

    private EscalationMatrixResponse mapToResponse(EscalationMatrix matrix) {
        EscalationMatrixResponse response = new EscalationMatrixResponse();
        response.setId(matrix.getId());
        response.setRelatedService(matrix.getRelatedService());
        response.setPriority(matrix.getPriority());
        response.setSupportLevel(matrix.getSupportLevel());
        response.setInitialAssignmentLevel(matrix.getInitialAssignmentLevel());
        response.setEscalateToLevel(matrix.getEscalateToLevel());
        response.setEscalationTimeMinutes(matrix.getEscalationTimeMinutes());
        response.setResponseTimeMinutes(matrix.getResponseTimeMinutes());
        response.setResolutionTimeMinutes(matrix.getResolutionTimeMinutes());
        response.setIsActive(matrix.getIsActive());
        response.setCreatedAt(matrix.getCreatedAt());
        response.setUpdatedAt(matrix.getUpdatedAt());
        return response;
    }
}

