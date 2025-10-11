package com.example.authservice.controller;

import com.example.authservice.dto.UserDto;
import com.example.authservice.service.UserService;
import com.example.authservice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserDto>> listUsers() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        List<UserDto> users = userService.listUsers(currentUserId);
        return ResponseEntity.ok(users);
    }
}

