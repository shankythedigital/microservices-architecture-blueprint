package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="audit_log")
public class AuditLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String action;
    private String entityName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String url;
    private String method;
    private LocalDateTime timestamp = LocalDateTime.now();

    // getters and setters
    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public Long getUserId(){return userId;}
    public void setUserId(Long v){this.userId=v;}
    public String getAction(){return action;}
    public void setAction(String v){this.action=v;}
    public String getEntityName(){return entityName;}
    public void setEntityName(String v){this.entityName=v;}
    public String getOldValue(){return oldValue;}
    public void setOldValue(String v){this.oldValue=v;}
    public String getNewValue(){return newValue;}
    public void setNewValue(String v){this.newValue=v;}
    public String getIpAddress(){return ipAddress;}
    public void setIpAddress(String v){this.ipAddress=v;}
    public String getUserAgent(){return userAgent;}
    public void setUserAgent(String v){this.userAgent=v;}
    public String getUrl(){return url;}
    public void setUrl(String v){this.url=v;}
    public String getMethod(){return method;}
    public void setMethod(String v){this.method=v;}
    public LocalDateTime getTimestamp(){return timestamp;}
    public void setTimestamp(LocalDateTime v){this.timestamp=v;}
}
