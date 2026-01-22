package com.example.helpdesk.service;

import com.example.helpdesk.dto.ChatbotMessageRequest;
import com.example.helpdesk.dto.ChatbotMessageResponse;
import com.example.helpdesk.entity.ChatbotMessage;
import com.example.helpdesk.entity.ChatbotSession;
import com.example.helpdesk.repository.ChatbotMessageRepository;
import com.example.helpdesk.repository.ChatbotSessionRepository;
import com.example.helpdesk.repository.FAQRepository;
import com.example.helpdesk.repository.ServiceKnowledgeRepository;
import com.example.helpdesk.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatbotService {
    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final FAQRepository faqRepository;
    private final ServiceKnowledgeRepository knowledgeRepository;

    public ChatbotService(
            ChatbotSessionRepository sessionRepository,
            ChatbotMessageRepository messageRepository,
            FAQRepository faqRepository,
            ServiceKnowledgeRepository knowledgeRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.faqRepository = faqRepository;
        this.knowledgeRepository = knowledgeRepository;
    }

    @Transactional
    public ChatbotMessageResponse processMessage(ChatbotMessageRequest request) {
        String userId = JwtUtil.getUserIdOrThrow();
        String username = JwtUtil.getUsernameOrThrow();

        // Get or create session
        ChatbotSession session;
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            session = sessionRepository.findBySessionId(request.getSessionId())
                    .orElseGet(() -> createNewSession(userId));
        } else {
            session = createNewSession(userId);
        }

        // Save user message
        ChatbotMessage userMessage = new ChatbotMessage();
        userMessage.setSession(session);
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        messageRepository.save(userMessage);

        // Generate bot response
        String botResponse = generateResponse(request.getMessage(), session);

        // Save bot response
        ChatbotMessage botMessage = new ChatbotMessage();
        botMessage.setSession(session);
        botMessage.setRole("assistant");
        botMessage.setContent(botResponse);
        messageRepository.save(botMessage);

        // Get conversation history
        List<ChatbotMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(session);
        List<ChatbotMessageResponse.ChatMessage> conversationHistory = messages.stream()
                .map(msg -> {
                    ChatbotMessageResponse.ChatMessage chatMsg = new ChatbotMessageResponse.ChatMessage();
                    chatMsg.setRole(msg.getRole());
                    chatMsg.setContent(msg.getContent());
                    chatMsg.setTimestamp(msg.getCreatedAt());
                    return chatMsg;
                })
                .collect(Collectors.toList());

        ChatbotMessageResponse response = new ChatbotMessageResponse();
        response.setSessionId(session.getSessionId());
        response.setResponse(botResponse);
        response.setConversationHistory(conversationHistory);
        response.setTimestamp(LocalDateTime.now());

        return response;
    }

    private ChatbotSession createNewSession(String userId) {
        ChatbotSession session = new ChatbotSession();
        session.setUserId(userId);
        session.setSessionId(UUID.randomUUID().toString());
        session.setIsActive(true);
        return sessionRepository.save(session);
    }

    private String generateResponse(String userMessage, ChatbotSession session) {
        String lowerMessage = userMessage.toLowerCase();

        // Check FAQs first
        List<com.example.helpdesk.entity.FAQ> matchingFAQs = faqRepository.searchByKeyword(userMessage);
        if (!matchingFAQs.isEmpty()) {
            return "Based on our FAQs, here's the answer:\n\n" + 
                   matchingFAQs.get(0).getAnswer() + 
                   "\n\nWould you like more information or do you have another question?";
        }

        // Check service knowledge
        List<com.example.helpdesk.entity.ServiceKnowledge> knowledge = knowledgeRepository.findAll();
        for (com.example.helpdesk.entity.ServiceKnowledge k : knowledge) {
            if (lowerMessage.contains(k.getTopic().toLowerCase()) || 
                k.getContent().toLowerCase().contains(lowerMessage)) {
                return "Here's information about " + k.getTopic() + ":\n\n" + 
                       k.getContent() + 
                       "\n\nIs there anything specific you'd like to know more about?";
            }
        }

        // Default responses based on keywords
        if (lowerMessage.contains("issue") || lowerMessage.contains("problem") || lowerMessage.contains("error")) {
            return "I understand you're experiencing an issue. To help you better, I can:\n" +
                   "1. Help you raise a new issue ticket\n" +
                   "2. Check existing FAQs related to your problem\n" +
                   "3. Guide you through troubleshooting steps\n\n" +
                   "Could you provide more details about the issue?";
        }

        if (lowerMessage.contains("auth") || lowerMessage.contains("login") || lowerMessage.contains("authentication")) {
            return "For authentication-related queries, I can help with:\n" +
                   "- Login issues\n" +
                   "- Password reset\n" +
                   "- Token management\n" +
                   "- User registration\n\n" +
                   "What specific authentication help do you need?";
        }

        if (lowerMessage.contains("asset") || lowerMessage.contains("inventory")) {
            return "For asset management queries, I can help with:\n" +
                   "- Asset creation and management\n" +
                   "- Asset tracking\n" +
                   "- AMC and warranty information\n" +
                   "- Asset compliance\n\n" +
                   "What would you like to know about assets?";
        }

        if (lowerMessage.contains("notification") || lowerMessage.contains("alert")) {
            return "For notification-related queries, I can help with:\n" +
                   "- Notification settings\n" +
                   "- Email notifications\n" +
                   "- Push notifications\n" +
                   "- Notification preferences\n\n" +
                   "What notification help do you need?";
        }

        // Generic response
        return "I'm here to help! I can assist you with:\n" +
               "- Raising and tracking issues\n" +
               "- Answering FAQs about our services\n" +
               "- Providing information about auth-service, notification-service, and asset-service\n" +
               "- Troubleshooting common problems\n\n" +
               "Could you please provide more details about what you need help with?";
    }

    public List<ChatbotMessageResponse> getSessionHistory(String sessionId) {
        ChatbotSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        List<ChatbotMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(session);
        List<ChatbotMessageResponse.ChatMessage> conversationHistory = messages.stream()
                .map(msg -> {
                    ChatbotMessageResponse.ChatMessage chatMsg = new ChatbotMessageResponse.ChatMessage();
                    chatMsg.setRole(msg.getRole());
                    chatMsg.setContent(msg.getContent());
                    chatMsg.setTimestamp(msg.getCreatedAt());
                    return chatMsg;
                })
                .collect(Collectors.toList());

        ChatbotMessageResponse response = new ChatbotMessageResponse();
        response.setSessionId(session.getSessionId());
        response.setConversationHistory(conversationHistory);
        response.setTimestamp(LocalDateTime.now());
        return List.of(response);
    }
}

