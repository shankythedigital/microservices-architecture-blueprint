package com.example.authservice.model;

import jakarta.persistence.*;
import java.util.Set;

/**
 * ✅ User Entity with embedded composite attributes and auto-generated primary key.
 * Uses logical hashes and project type for uniqueness constraints.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username_hash", "project_type"}),
        @UniqueConstraint(columnNames = {"email_hash", "project_type"}),
        @UniqueConstraint(columnNames = {"mobile_hash", "project_type"})
    }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;  // Primary key

    @Embedded
    private UserId compositeId; // Logical composite identifiers (hashes + projectType)

    @Column(name = "username_enc", length = 2048)
    private String usernameEnc;

    @Column(name = "email_enc", length = 2048)
    private String emailEnc;

    @Column(name = "mobile_enc", length = 2048)
    private String mobileEnc;

    private String password;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"), // uses PK
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserDetailMaster detail;

    // ----------------------
    // Constructors
    // ----------------------
    public User() {}

    public User(UserId compositeId, String usernameEnc, String emailEnc, String mobileEnc, String password, Boolean enabled) {
        this.compositeId = compositeId;
        this.usernameEnc = usernameEnc;
        this.emailEnc = emailEnc;
        this.mobileEnc = mobileEnc;
        this.password = password;
        this.enabled = enabled;
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UserId getCompositeId() { return compositeId; }
    public void setCompositeId(UserId compositeId) { this.compositeId = compositeId; }

    public String getUsernameEnc() { return usernameEnc; }
    public void setUsernameEnc(String usernameEnc) { this.usernameEnc = usernameEnc; }

    public String getEmailEnc() { return emailEnc; }
    public void setEmailEnc(String emailEnc) { this.emailEnc = emailEnc; }

    public String getMobileEnc() { return mobileEnc; }
    public void setMobileEnc(String mobileEnc) { this.mobileEnc = mobileEnc; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public UserDetailMaster getDetail() { return detail; }
    public void setDetail(UserDetailMaster detail) { this.detail = detail; }

    // ----------------------
    // Convenience Accessors
    // ----------------------
    public String getUsernameHash() { return compositeId != null ? compositeId.getUsernameHash() : null; }
    public String getEmailHash() { return compositeId != null ? compositeId.getEmailHash() : null; }
    public String getMobileHash() { return compositeId != null ? compositeId.getMobileHash() : null; }
    public String getProjectType() { return compositeId != null ? compositeId.getProjectType() : null; }
}


