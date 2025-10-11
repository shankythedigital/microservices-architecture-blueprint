package com.example.authservice.model;

import com.example.authservice.crypto.JpaAttributeEncryptor;
import com.example.authservice.util.HmacUtil;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_detail_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username_hash"}),
        @UniqueConstraint(columnNames = {"email_hash"}),
        @UniqueConstraint(columnNames = {"mobile_hash"})
    }
)
public class UserDetailMaster extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Encrypted + HMAC username
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", nullable = false, length = 2048)
    private String username;

    @Column(name = "username_hash", nullable = false, unique = true, length = 512)
    private String usernameHash;

    // Encrypted + HMAC email
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "email_enc", length = 2048)
    private String email;

    @Column(name = "email_hash", unique = true, length = 512)
    private String emailHash;

    // Encrypted + HMAC mobile
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    @Column(name = "mobile_hash", unique = true, length = 512)
    private String mobileHash;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "employee_id_enc", length = 1024)
    private String employeeId;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "login_retry")
    private Integer loginRetry = 0;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    // ✅ Auto-compute HMAC values before insert/update
    @PrePersist
    @PreUpdate
    public void computeHashes() {
        if (this.username != null) this.usernameHash = HmacUtil.hmacHex(this.username);
        if (this.email != null) this.emailHash = HmacUtil.hmacHex(this.email);
        if (this.mobile != null) this.mobileHash = HmacUtil.hmacHex(this.mobile);
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUsernameHash() { return usernameHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailHash() { return emailHash; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getMobileHash() { return mobileHash; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDateTime loginDate) { this.loginDate = loginDate; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public Integer getLoginRetry() { return loginRetry; }
    public void setLoginRetry(Integer loginRetry) { this.loginRetry = loginRetry; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
}

