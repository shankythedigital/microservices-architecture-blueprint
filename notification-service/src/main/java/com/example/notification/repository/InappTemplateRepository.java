package com.example.notification.repository;

import com.example.notification.entity.templates.InappTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InappTemplateRepository extends JpaRepository<InappTemplateMaster, Long> {
    Optional<InappTemplateMaster> findByTemplateCode(String templateCode);
}
