package com.example.helpdesk.repository;

import com.example.helpdesk.entity.ChatbotMessage;
import com.example.helpdesk.entity.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    List<ChatbotMessage> findBySessionOrderByCreatedAtAsc(ChatbotSession session);
}

