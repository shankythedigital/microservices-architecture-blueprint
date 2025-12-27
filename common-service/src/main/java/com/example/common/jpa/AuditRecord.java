package com.example.common.jpa;

import java.time.LocalDateTime;

/**
 * Lightweight audit record POJO - for projects that want to store audit logs.
 */
public class AuditRecord {
    private LocalDateTime timestamp;
    private String username;
    private String ip;
    private String userAgent;
    private String url;
    private String method;
    private String details;

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
