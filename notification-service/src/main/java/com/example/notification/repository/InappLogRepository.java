package com.example.notification.repository;

import com.example.notification.entity.InappLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InappLogRepository extends JpaRepository<InappLog, Long> {
    
    /**
     * Find all in-app notifications for a specific user within a date range
     */
    List<InappLog> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, LocalDateTime afterDate);
    
    /**
     * Find all in-app notifications for a specific user
     */
    List<InappLog> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find notification by ID and userId (for security - ensure user can only mark their own notifications)
     */
    Optional<InappLog> findByIdAndUserId(Long id, String userId);

    /**
     * Find notifications by userId and createdAt after date
     */
    List<InappLog> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime afterDate);
}
