package com.example.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    @Column(length = 1000)
    private String userAgent;
    private String url;
    private String httpMethod;
    private String username;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getIpAddress(){return ipAddress;}
    public void setIpAddress(String ipAddress){this.ipAddress=ipAddress;}
    public String getUserAgent(){return userAgent;}
    public void setUserAgent(String userAgent){this.userAgent=userAgent;}
    public String getUrl(){return url;}
    public void setUrl(String url){this.url=url;}
    public String getHttpMethod(){return httpMethod;}
    public void setHttpMethod(String httpMethod){this.httpMethod=httpMethod;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
    public String getUsername(){return username;}
    public void setUsername(String username){this.username=username;}
}
