package com.example.notification.repository;

import com.example.notification.entity.InappLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InappLogRepository extends JpaRepository<InappLog, Long> {
}
