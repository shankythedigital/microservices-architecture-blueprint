package com.example.authservice.model;

import com.example.common.converter.JpaAttributeEncryptor;
import com.example.common.util.HmacUtil;
import jakarta.persistence.*;

import com.example.common.jpa.BaseEntity;
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

    // Address fields
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "pincode_enc", length = 1024)
    private String pincode;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "city_enc", length = 1024)
    private String city;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "state_enc", length = 1024)
    private String state;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "country_enc", length = 1024)
    private String country;

    // Terms & Conditions acceptance
    @Column(name = "accept_tc")
    private Boolean acceptTc = false;

    // Country code for mobile validation
    @Column(name = "country_code", length = 10)
    private String countryCode;

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

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getAcceptTc() { return acceptTc; }
    public void setAcceptTc(Boolean acceptTc) { this.acceptTc = acceptTc; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}

