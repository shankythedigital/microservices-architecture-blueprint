package com.example.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * ✅ Embeddable logical composite key data for User entity.
 * Note: user_id is excluded from persistence control (managed by User).
 */
@Embeddable
public class UserId implements Serializable {

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;  // Reference only; not persisted by this embeddable

    @Column(name = "username_hash", length = 512)
    private String usernameHash;

    @Column(name = "email_hash", length = 512)
    private String emailHash;

    @Column(name = "mobile_hash", length = 512)
    private String mobileHash;

    @Column(name = "project_type", length = 50)
    private String projectType;

    public UserId() {}

    // Overloaded constructor without userId (used during registration)
    public UserId(String usernameHash, String emailHash, String mobileHash, String projectType) {
        this.usernameHash = usernameHash;
        this.emailHash = emailHash;
        this.mobileHash = mobileHash;
        this.projectType = projectType;
    }

    // Full constructor
    public UserId(Long userId, String usernameHash, String emailHash, String mobileHash, String projectType) {
        this.userId = userId;
        this.usernameHash = usernameHash;
        this.emailHash = emailHash;
        this.mobileHash = mobileHash;
        this.projectType = projectType;
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsernameHash() { return usernameHash; }
    public void setUsernameHash(String usernameHash) { this.usernameHash = usernameHash; }

    public String getEmailHash() { return emailHash; }
    public void setEmailHash(String emailHash) { this.emailHash = emailHash; }

    public String getMobileHash() { return mobileHash; }
    public void setMobileHash(String mobileHash) { this.mobileHash = mobileHash; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(usernameHash, that.usernameHash)
                && Objects.equals(emailHash, that.emailHash)
                && Objects.equals(mobileHash, that.mobileHash)
                && Objects.equals(projectType, that.projectType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, usernameHash, emailHash, mobileHash, projectType);
    }
}


