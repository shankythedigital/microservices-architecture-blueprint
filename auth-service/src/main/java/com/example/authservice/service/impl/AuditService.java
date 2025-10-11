package com.example.authservice.service.impl;

import com.example.authservice.model.AuditLog;
import com.example.authservice.repository.AuditLogRepository;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    @Autowired private AuditLogRepository repo;

    public void log(Long userId, String action, String entity,
                    String oldValue, String newValue,
                    String ip, String ua) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityName(entity);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);

        // If null passed from caller, fallback to RequestContext
        log.setIpAddress(ip != null ? ip : RequestContext.getIp());
        log.setUserAgent(ua != null ? ua : RequestContext.getUserAgent());
        log.setUrl(RequestContext.getUrl());
        log.setMethod(RequestContext.getMethod());

        repo.save(log);
    }
}
