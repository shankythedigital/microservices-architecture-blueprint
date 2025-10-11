package com.example.notification.repository;

import com.example.notification.entity.WhatsappLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsappLogRepository extends JpaRepository<WhatsappLog, Long> {
}
