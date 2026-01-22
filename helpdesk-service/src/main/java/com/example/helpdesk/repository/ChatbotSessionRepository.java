package com.example.helpdesk.repository;

import com.example.helpdesk.entity.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {
    Optional<ChatbotSession> findBySessionId(String sessionId);
    List<ChatbotSession> findByUserId(String userId);
    List<ChatbotSession> findByUserIdAndIsActive(String userId, Boolean isActive);
}

