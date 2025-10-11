package com.example.notification.repository;

import com.example.notification.entity.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
}
