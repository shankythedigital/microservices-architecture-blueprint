package com.example.authservice.controller;

import com.example.authservice.model.AuditLog;
import com.example.authservice.service.impl.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    @Autowired
    private AuditLogService auditLogService;

    // List logs (all)
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(auditLogService.findLogs(userId, action, url, method, from, to));
    }

    // Paged logs
    @GetMapping("/logs/paged")
    public ResponseEntity<Page<AuditLog>> getLogsPaged(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.findLogsPaged(userId, action, url, method, from, to, pageable));
    }

    // CSV export
    @GetMapping("/logs/csv")
    public ResponseEntity<byte[]> getLogsCsv(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AuditLog> logs = auditLogService.findLogs(userId, action, url, method, from, to);
        String csv = auditLogService.exportToCsv(logs);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    // Excel export
    @GetMapping("/logs/excel")
    public ResponseEntity<byte[]> getLogsExcel(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AuditLog> logs = auditLogService.findLogs(userId, action, url, method, from, to);
        byte[] bytes = auditLogService.exportToExcel(logs);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}

