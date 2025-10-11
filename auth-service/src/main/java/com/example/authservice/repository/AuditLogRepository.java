package com.example.authservice.repository;

import com.example.authservice.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:userId IS NULL OR a.userId = :userId) " +
           "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
           "AND (:from IS NULL OR a.timestamp >= :from) " +
           "AND (:to IS NULL OR a.timestamp <= :to)")
    List<AuditLog> searchLogs(@Param("userId") Long userId,
                              @Param("action") String action,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:userId IS NULL OR a.userId = :userId) " +
           "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
           "AND (:from IS NULL OR a.timestamp >= :from) " +
           "AND (:to IS NULL OR a.timestamp <= :to)")
    Page<AuditLog> searchLogsPaged(@Param("userId") Long userId,
                                   @Param("action") String action,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   Pageable pageable);

@Query("SELECT a FROM AuditLog a " +
       "WHERE (:userId IS NULL OR a.userId = :userId) " +
       "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
       "AND (:url IS NULL OR a.url LIKE %:url%) " +
       "AND (:method IS NULL OR UPPER(a.method) = UPPER(:method)) " +
       "AND (:from IS NULL OR a.timestamp >= :from) " +
       "AND (:to IS NULL OR a.timestamp <= :to)")
List<AuditLog> searchLogs(@Param("userId") Long userId,
                          @Param("action") String action,
                          @Param("url") String url,
                          @Param("method") String method,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);

@Query("SELECT a FROM AuditLog a " +
       "WHERE (:userId IS NULL OR a.userId = :userId) " +
       "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
       "AND (:url IS NULL OR a.url LIKE %:url%) " +
       "AND (:method IS NULL OR UPPER(a.method) = UPPER(:method)) " +
       "AND (:from IS NULL OR a.timestamp >= :from) " +
       "AND (:to IS NULL OR a.timestamp <= :to)")
Page<AuditLog> searchLogsPaged(@Param("userId") Long userId,
                               @Param("action") String action,
                               @Param("url") String url,
                               @Param("method") String method,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               Pageable pageable);

}
