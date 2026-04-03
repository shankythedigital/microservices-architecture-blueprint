package com.example.helpdesk.service;

import com.example.helpdesk.dto.QueryAnswerRequest;
import com.example.helpdesk.dto.QueryRequest;
import com.example.helpdesk.dto.QueryResponse;
import com.example.helpdesk.entity.Query;
import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.repository.QueryRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QueryService {
    private final QueryRepository queryRepository;

    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Transactional
    public QueryResponse createQuery(QueryRequest request) {
        String reporterUserId = JwtUtil.getUserIdOrThrow();
        Optional<Long> loginUserId = JwtUtil.getLoginUserId();

        Query query = new Query();
        query.setQuestion(request.getQuestion());
        query.setRelatedService(request.getRelatedService());
        query.setStatus(QueryStatus.PENDING);
        query.setAskedBy(reporterUserId);
        query.setCreatedBy(reporterUserId);
        loginUserId.ifPresent(query::setLoginUserId);

        Query saved = queryRepository.save(query);
        return mapToResponse(saved);
    }

    public List<QueryResponse> getAllQueries() {
        return queryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public QueryResponse getQueryById(Long id) {
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));
        return mapToResponse(query);
    }

    public List<QueryResponse> getQueriesByStatus(QueryStatus status) {
        return queryRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<QueryResponse> getQueriesByService(com.example.helpdesk.enums.RelatedService service) {
        return queryRepository.findByRelatedService(service).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lists queries for the current user. Uses {@code login_user_id} because {@code asked_by_enc} is AES-GCM
     * (random IV) and cannot be matched with {@code findByAskedBy} in SQL.
     */
    public List<QueryResponse> getMyQueries() {
        Long uid = JwtUtil.getLoginUserId()
                .orElseThrow(() -> new RuntimeException("Authenticated user id missing or not numeric in token"));
        return queryRepository.findByLoginUserIdOrderByCreatedAtDesc(uid).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QueryResponse answerQuery(Long id, QueryAnswerRequest request) {
        String username = JwtUtil.getUsernameOrThrow();
        
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));
        query.setAnswer(request.getAnswer());
        query.setStatus(QueryStatus.ANSWERED);
        query.setAnsweredBy(username);
        query.setUpdatedBy(username);
        
        Query saved = queryRepository.save(query);
        return mapToResponse(saved);
    }

    @Transactional
    public QueryResponse closeQuery(Long id) {
        String username = JwtUtil.getUsernameOrThrow();
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Query not found with id: " + id));
        query.setStatus(QueryStatus.CLOSED);
        query.setUpdatedBy(username);
        Query saved = queryRepository.save(query);
        return mapToResponse(saved);
    }

    private QueryResponse mapToResponse(Query query) {
        QueryResponse response = new QueryResponse();
        response.setId(query.getId());
        response.setQuestion(query.getQuestion());
        response.setAnswer(query.getAnswer());
        response.setStatus(query.getStatus());
        response.setRelatedService(query.getRelatedService());
        response.setAskedBy(query.getAskedBy());
        response.setAnsweredBy(query.getAnsweredBy());
        response.setCreatedAt(query.getCreatedAt());
        response.setUpdatedAt(query.getUpdatedAt());
        response.setAnsweredAt(query.getAnsweredAt());
        response.setLoginUserId(query.getLoginUserId());
        return response;
    }
}

