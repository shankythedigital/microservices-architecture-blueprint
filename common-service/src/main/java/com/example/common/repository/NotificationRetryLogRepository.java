package com.example.common.repository;

import com.example.common.entity.NotificationRetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRetryLogRepository extends JpaRepository<NotificationRetryLog, Long> {

    List<NotificationRetryLog> findByProcessedFalseOrderByCreatedAtAsc();
}


