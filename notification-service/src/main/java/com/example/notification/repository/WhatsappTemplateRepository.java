package com.example.notification.repository;

import com.example.notification.entity.templates.WhatsappTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WhatsappTemplateRepository extends JpaRepository<WhatsappTemplateMaster, Long> {
    Optional<WhatsappTemplateMaster> findByTemplateCode(String templateCode);
}
