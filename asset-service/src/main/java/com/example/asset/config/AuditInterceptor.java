package com.example.asset.config;

import com.example.asset.entity.AuditLog;
import com.example.asset.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.LocalDateTime;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogRepository repo;
    public AuditInterceptor(AuditLogRepository repo){ this.repo = repo; }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) {
        try {
            AuditLog log = new AuditLog();
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setUrl(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            var principal = request.getUserPrincipal();
            if(principal!=null) log.setUsername(principal.getName());
            log.setCreatedAt(LocalDateTime.now());
            repo.save(log);
        } catch (Exception ignored){}
        return true;
    }
}
