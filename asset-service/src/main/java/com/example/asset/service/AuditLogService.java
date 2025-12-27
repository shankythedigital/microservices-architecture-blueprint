
package com.example.asset.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

    void logEvent(
            String username,
            String eventMessage,
            HttpServletRequest request
    );
}


