
package com.example.authservice.controller;

import com.example.authservice.dto.UserDto;
import com.example.authservice.service.UserService;
import com.example.authservice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    // GET /api/users/me  -> returns current user's profile
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserDto dto = userService.getMyProfile(currentUserId);
        return ResponseEntity.ok(dto);
    }

    // GET /api/users/{id} -> admin or self
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserDto dto = userService.getUserProfile(id, currentUserId);
        return ResponseEntity.ok(dto);
    }
}

