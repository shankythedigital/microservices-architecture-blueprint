package com.example.helpdesk.service;

import com.example.helpdesk.dto.ServiceKnowledgeRequest;
import com.example.helpdesk.dto.ServiceKnowledgeResponse;
import com.example.helpdesk.entity.ServiceKnowledge;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.repository.ServiceKnowledgeRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceKnowledgeService {
    private final ServiceKnowledgeRepository knowledgeRepository;

    public ServiceKnowledgeService(ServiceKnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Transactional
    public ServiceKnowledgeResponse createKnowledge(ServiceKnowledgeRequest request) {
        String username = JwtUtil.getUsernameOrThrow();

        ServiceKnowledge knowledge = new ServiceKnowledge();
        knowledge.setService(request.getService());
        knowledge.setTopic(request.getTopic());
        knowledge.setContent(request.getContent());
        knowledge.setCategory(request.getCategory());
        knowledge.setApiEndpoints(request.getApiEndpoints());
        knowledge.setCommonIssues(request.getCommonIssues());
        knowledge.setTroubleshootingSteps(request.getTroubleshootingSteps());
        knowledge.setCreatedBy(username);

        ServiceKnowledge saved = knowledgeRepository.save(knowledge);
        return mapToResponse(saved);
    }

    public List<ServiceKnowledgeResponse> getAllKnowledge() {
        return knowledgeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServiceKnowledgeResponse getKnowledgeById(Long id) {
        ServiceKnowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge not found with id: " + id));
        return mapToResponse(knowledge);
    }

    public List<ServiceKnowledgeResponse> getKnowledgeByService(RelatedService service) {
        return knowledgeRepository.findByService(service).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ServiceKnowledgeResponse> searchKnowledge(RelatedService service, String keyword) {
        return knowledgeRepository.searchByServiceAndKeyword(service, keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceKnowledgeResponse updateKnowledge(Long id, ServiceKnowledgeRequest request) {
        String username = JwtUtil.getUsernameOrThrow();
        ServiceKnowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge not found with id: " + id));
        knowledge.setService(request.getService());
        knowledge.setTopic(request.getTopic());
        knowledge.setContent(request.getContent());
        knowledge.setCategory(request.getCategory());
        knowledge.setApiEndpoints(request.getApiEndpoints());
        knowledge.setCommonIssues(request.getCommonIssues());
        knowledge.setTroubleshootingSteps(request.getTroubleshootingSteps());
        knowledge.setUpdatedBy(username);
        
        ServiceKnowledge saved = knowledgeRepository.save(knowledge);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteKnowledge(Long id) {
        if (!knowledgeRepository.existsById(id)) {
            throw new RuntimeException("Knowledge not found with id: " + id);
        }
        knowledgeRepository.deleteById(id);
    }

    private ServiceKnowledgeResponse mapToResponse(ServiceKnowledge knowledge) {
        ServiceKnowledgeResponse response = new ServiceKnowledgeResponse();
        response.setId(knowledge.getId());
        response.setService(knowledge.getService());
        response.setTopic(knowledge.getTopic());
        response.setContent(knowledge.getContent());
        response.setCategory(knowledge.getCategory());
        response.setApiEndpoints(knowledge.getApiEndpoints());
        response.setCommonIssues(knowledge.getCommonIssues());
        response.setTroubleshootingSteps(knowledge.getTroubleshootingSteps());
        response.setCreatedAt(knowledge.getCreatedAt());
        response.setUpdatedAt(knowledge.getUpdatedAt());
        return response;
    }
}

