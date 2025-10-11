package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ✅ UserDto
 * Safe data transfer object for exposing user information.
 * Used for:
 *  - GET /users/me
 *  - GET /users/{id}
 *  - GET /admin/users
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long userId;             // System-generated user ID
    private String username;         // Decrypted username
    private String email;            // Decrypted email
    private String mobile;           // Decrypted mobile
    private String projectType;      // ECOM / ASSET / etc.
    private Boolean enabled;         // Account active?
    private Set<String> roles;       // ROLE_USER / ROLE_ADMIN
    private LocalDateTime lastLoginDate; // Last login timestamp

    public UserDto() {}

    public UserDto(Long userId, String username, String email, String mobile,
                   String projectType, Boolean enabled, Set<String> roles, LocalDateTime lastLoginDate) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.projectType = projectType;
        this.enabled = enabled;
        this.roles = roles;
        this.lastLoginDate = lastLoginDate;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    // -------------------------
    // Builder Pattern (Optional)
    // -------------------------
    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    public static class UserDtoBuilder {
        private final UserDto dto = new UserDto();

        public UserDtoBuilder userId(Long id) { dto.setUserId(id); return this; }
        public UserDtoBuilder username(String v) { dto.setUsername(v); return this; }
        public UserDtoBuilder email(String v) { dto.setEmail(v); return this; }
        public UserDtoBuilder mobile(String v) { dto.setMobile(v); return this; }
        public UserDtoBuilder projectType(String v) { dto.setProjectType(v); return this; }
        public UserDtoBuilder enabled(Boolean v) { dto.setEnabled(v); return this; }
        public UserDtoBuilder roles(Set<String> v) { dto.setRoles(v); return this; }
        public UserDtoBuilder lastLoginDate(LocalDateTime v) { dto.setLastLoginDate(v); return this; }

        public UserDto build() { return dto; }
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", projectType='" + projectType + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                ", lastLoginDate=" + lastLoginDate +
                '}';
    }
}


