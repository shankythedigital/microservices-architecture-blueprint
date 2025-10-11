
package com.example.authservice.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDetailDto {
    public Long userId;
    public String username;   // decrypted by JPA converter if used on entity
    public String email;
    public String mobile;
    public String employeeId;
    public LocalDateTime loginDate;
    public LocalDateTime lastLoginDate;
    public Integer failedAttempts;
    public Boolean accountLocked;
}
