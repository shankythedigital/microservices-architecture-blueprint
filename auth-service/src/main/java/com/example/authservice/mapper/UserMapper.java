
package com.example.authservice.mapper;

import com.example.authservice.dto.UserDto;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.model.UserDetailMaster;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ✅ UserMapper
 * Safely converts between entity models and DTOs.
 * Now aligned with the updated User model where projectType is inside compositeId.
 */
@Component
public class UserMapper {

    /**
     * Convert entity → DTO (safe for API responses)
     */
    public UserDto toDto(User user, UserDetailMaster detail) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(detail != null ? detail.getUsername() : null);
        dto.setEmail(detail != null ? detail.getEmail() : null);
        dto.setMobile(detail != null ? detail.getMobile() : null);

        // ✅ Access projectType from compositeId
        dto.setProjectType(user.getCompositeId() != null ? user.getCompositeId().getProjectType() : null);

        dto.setEnabled(user.getEnabled());
        dto.setRoles(mapRoles(user.getRoles()));
        dto.setLastLoginDate(detail != null ? detail.getLastLoginDate() : null);

        return dto;
    }

    /**
     * Convert list of Role entities → set of role names
     */
    private Set<String> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Optional: Convert DTO → entity (used for admin updates or registration)
     */
    public void updateEntityFromDto(UserDto dto, User user, UserDetailMaster detail) {
        if (dto == null || user == null || detail == null) return;

        // Basic safe updates
        Optional.ofNullable(dto.getUsername()).ifPresent(detail::setUsername);
        Optional.ofNullable(dto.getEmail()).ifPresent(detail::setEmail);
        Optional.ofNullable(dto.getMobile()).ifPresent(detail::setMobile);
        Optional.ofNullable(dto.getEnabled()).ifPresent(user::setEnabled);

        // ✅ Safely update projectType in the embedded key
        if (dto.getProjectType() != null && user.getCompositeId() != null) {
            user.getCompositeId().setProjectType(dto.getProjectType());
        }

        // Roles are handled via RoleRepository in the service layer
    }
}


