
package com.example.asset.service.impl;

import com.example.asset.entity.AuditLog;
import com.example.asset.repository.AuditLogRepository;
import com.example.asset.service.AuditLogService;
import com.example.asset.util.AuditLoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditRepo;

    public AuditLogServiceImpl(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @Override
    public void logEvent(String username, String eventMessage, HttpServletRequest req) {

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setEventMessage(eventMessage);

        log.setIpAddress(AuditLoggingUtil.getClientIP(req));
        log.setUserAgent(AuditLoggingUtil.getUserAgent(req));
        log.setUrl(AuditLoggingUtil.getUrl(req));
        log.setHttpMethod(AuditLoggingUtil.getMethod(req));

        auditRepo.save(log);
    }
}

