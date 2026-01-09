package com.example.notification.repository;

import com.example.notification.entity.InappLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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
     * Count unread notifications for a user within a date range
     * (Assuming read status will be added later)
     */
    @Query("SELECT COUNT(i) FROM InappLog i WHERE i.userId = :userId AND i.createdAt >= :afterDate")
    Long countByUserIdAndCreatedAtAfter(@Param("userId") String userId, @Param("afterDate") LocalDateTime afterDate);
}
