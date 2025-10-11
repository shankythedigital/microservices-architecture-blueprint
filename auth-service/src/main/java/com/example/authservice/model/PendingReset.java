package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="pending_resets")
public class PendingReset extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String type; // "MPIN", "EMAIL", "MOBILE"
    private String resetToken;
    private LocalDateTime expiresAt;

    // getters/setters
    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public Long getUserId(){return userId;}
    public void setUserId(Long v){this.userId=v;}
    public String getType(){return type;}
    public void setType(String v){this.type=v;}
    public String getResetToken(){return resetToken;}
    public void setResetToken(String v){this.resetToken=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(LocalDateTime v){this.expiresAt=v;}
}
