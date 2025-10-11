package com.example.notification.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    private String action; // CREATE, UPDATE, DELETE, READ

    @Column(columnDefinition = "JSON")
    private String oldValue;

    @Column(columnDefinition = "JSON")
    private String newValue;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    private String url;

    private String httpMethod;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
