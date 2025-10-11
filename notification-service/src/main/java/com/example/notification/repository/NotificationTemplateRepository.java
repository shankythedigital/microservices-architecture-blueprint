package com.example.notification.repository;

import com.example.notification.entity.templates.NotificationTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateMaster, Long> {
    Optional<NotificationTemplateMaster> findByTemplateCode(String templateCode);
}
