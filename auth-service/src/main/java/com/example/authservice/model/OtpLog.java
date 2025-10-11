package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="otp_log")
public class OtpLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="mobile_hash", length=512) private String mobileHash;
    @Column(name="otp_hash", length=512) private String otpHash;
    private LocalDateTime expiresAt;
    private Boolean used = false;

    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public String getMobileHash(){return mobileHash;}
    public void setMobileHash(String v){this.mobileHash=v;}
    public String getOtpHash(){return otpHash;}
    public void setOtpHash(String v){this.otpHash=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(LocalDateTime v){this.expiresAt=v;}
    public boolean isUsed() { return used; }
    public void setUsed(Boolean v){this.used=v;}
}
