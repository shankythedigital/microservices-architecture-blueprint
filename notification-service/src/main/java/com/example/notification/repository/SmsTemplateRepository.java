package com.example.notification.repository;

import com.example.notification.entity.templates.SmsTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplateMaster, Long> {
    Optional<SmsTemplateMaster> findByTemplateCode(String templateCode);
}
