package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> send(@Valid @RequestBody NotificationRequest req) {
        service.enqueue(req);
        
        return ResponseEntity.accepted().body(req.getChannel() + " Notification accepted");
    }
}
