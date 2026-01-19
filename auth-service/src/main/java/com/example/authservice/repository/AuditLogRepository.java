package com.example.authservice.repository;

import com.example.authservice.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /**
     * Find audit logs by userId
     */
    List<AuditLog> findByUserId(Long userId);

    /**
     * Find audit logs by userId with pagination
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs by action (case-insensitive)
     */
    List<AuditLog> findByActionIgnoreCase(String action);

    /**
     * Find audit logs by userId and action
     */
    List<AuditLog> findByUserIdAndActionIgnoreCase(Long userId, String action);

    /**
     * Find audit logs by timestamp range
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Find audit logs by userId and timestamp range
     */
    List<AuditLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime from, LocalDateTime to);

    /**
     * Find audit logs by URL containing
     */
    List<AuditLog> findByUrlContainingIgnoreCase(String url);

    /**
     * Find audit logs by method (case-insensitive)
     */
    List<AuditLog> findByMethodIgnoreCase(String method);

    /**
     * Find audit logs by userId, URL containing, and method
     */
    List<AuditLog> findByUserIdAndUrlContainingIgnoreCaseAndMethodIgnoreCase(
            Long userId, String url, String method);

    /**
     * Find audit logs by timestamp range with pagination
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Find audit logs by userId and timestamp range with pagination
     */
    Page<AuditLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

}
