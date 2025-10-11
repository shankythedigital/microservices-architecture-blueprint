package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="sessions")
public class Session extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name="user_id") private User user;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;
    private Boolean revoked = false;
    private String deviceInfo;

    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public User getUser(){return user;}
    public void setUser(User v){this.user=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(LocalDateTime v){this.expiresAt=v;}
    public Boolean isRevoked(){return revoked;}
    public void setRevoked(Boolean v){this.revoked=v;}
    public String getDeviceInfo(){return deviceInfo;}
    public void setDeviceInfo(String v){this.deviceInfo=v;}
}
