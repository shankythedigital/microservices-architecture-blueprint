package com.example.helpdesk.service;

import com.example.helpdesk.dto.FAQRequest;
import com.example.helpdesk.dto.FAQResponse;
import com.example.helpdesk.entity.FAQ;
import com.example.helpdesk.enums.RelatedService;
import com.example.helpdesk.repository.FAQRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FAQService {
    private final FAQRepository faqRepository;

    public FAQService(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Transactional
    public FAQResponse createFAQ(FAQRequest request) {
        String username = JwtUtil.getUsernameOrThrow();

        FAQ faq = new FAQ();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setRelatedService(request.getRelatedService());
        faq.setCategory(request.getCategory());
        faq.setCreatedBy(username);

        FAQ saved = faqRepository.save(faq);
        return mapToResponse(saved);
    }

    public List<FAQResponse> getAllFAQs() {
        return faqRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FAQResponse getFAQById(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        incrementViewCount(faq);
        return mapToResponse(faq);
    }

    public List<FAQResponse> getFAQsByService(RelatedService service) {
        return faqRepository.findByRelatedService(service).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FAQResponse> getFAQsByCategory(String category) {
        return faqRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FAQResponse> searchFAQs(String keyword) {
        return faqRepository.searchByKeyword(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FAQResponse> searchFAQsByService(RelatedService service, String keyword) {
        return faqRepository.searchByServiceAndKeyword(service, keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FAQResponse updateFAQ(Long id, FAQRequest request) {
        String username = JwtUtil.getUsernameOrThrow();
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setRelatedService(request.getRelatedService());
        faq.setCategory(request.getCategory());
        faq.setUpdatedBy(username);
        FAQ saved = faqRepository.save(faq);
        return mapToResponse(saved);
    }

    @Transactional
    public void markFAQAsHelpful(Long id) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        faq.setHelpfulCount(faq.getHelpfulCount() + 1);
        faqRepository.save(faq);
    }

    @Transactional
    public void deleteFAQ(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ not found with id: " + id);
        }
        faqRepository.deleteById(id);
    }

    @Transactional
    private void incrementViewCount(FAQ faq) {
        faq.setViewCount(faq.getViewCount() + 1);
        faqRepository.save(faq);
    }

    private FAQResponse mapToResponse(FAQ faq) {
        FAQResponse response = new FAQResponse();
        response.setId(faq.getId());
        response.setQuestion(faq.getQuestion());
        response.setAnswer(faq.getAnswer());
        response.setRelatedService(faq.getRelatedService());
        response.setCategory(faq.getCategory());
        response.setViewCount(faq.getViewCount());
        response.setHelpfulCount(faq.getHelpfulCount());
        response.setCreatedAt(faq.getCreatedAt());
        response.setUpdatedAt(faq.getUpdatedAt());
        return response;
    }
}

