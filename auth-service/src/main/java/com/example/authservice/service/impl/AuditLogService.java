package com.example.authservice.service.impl;

import com.example.authservice.model.AuditLog;
import com.example.authservice.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {
    @Autowired
    private AuditLogRepository repo;

    /**
     * Build Specification for audit log search with optional parameters
     */
    private Specification<AuditLog> buildSpecification(Long userId, String action, String url, String method,
                                                        LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("action")), action.toLowerCase()));
            }
            if (url != null && !url.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("url")), "%" + url.toLowerCase() + "%"));
            }
            if (method != null && !method.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("method")), method.toUpperCase()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * DB-level filtered list
     */
    public List<AuditLog> findLogs(Long userId, String action, String url, String method,
                                   LocalDateTime from, LocalDateTime to) {
        Specification<AuditLog> spec = buildSpecification(userId, action, url, method, from, to);
        return repo.findAll(spec);
    }

    /**
     * DB-level paged search
     */
    public Page<AuditLog> findLogsPaged(Long userId, String action, String url, String method,
                                        LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<AuditLog> spec = buildSpecification(userId, action, url, method, from, to);
        return repo.findAll(spec, pageable);
    }

    public String exportToCsv(List<AuditLog> logs) {
        String header = "ID,UserId,Action,Entity,OldValue,NewValue,IP,UserAgent,URL,Method,Timestamp\n";
        StringBuilder sb = new StringBuilder(header);
        for (AuditLog l : logs) {
            sb.append(nullSafe(l.getId())).append(",")
              .append(nullSafe(l.getUserId())).append(",")
              .append(escapeCSV(l.getAction())).append(",")
              .append(escapeCSV(l.getEntityName())).append(",")
              .append(escapeCSV(l.getOldValue())).append(",")
              .append(escapeCSV(l.getNewValue())).append(",")
              .append(escapeCSV(l.getIpAddress())).append(",")
              .append(escapeCSV(l.getUserAgent())).append(",")
              .append(escapeCSV(l.getUrl())).append(",")
              .append(escapeCSV(l.getMethod())).append(",")
              .append(nullSafe(l.getTimestamp()))
              .append("\n");
        }
        return sb.toString();
    }

    public byte[] exportToExcel(List<AuditLog> logs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("AuditLogs");
            String[] columns = {"ID","UserId","Action","Entity","OldValue","NewValue","IP","UserAgent","URL","Method","Timestamp"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) headerRow.createCell(i).setCellValue(columns[i]);

            int rowIdx = 1;
            for (AuditLog l : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getId() != null ? l.getId() : -1);
                row.createCell(1).setCellValue(l.getUserId() != null ? l.getUserId() : -1);
                row.createCell(2).setCellValue(nullSafeString(l.getAction()));
                row.createCell(3).setCellValue(nullSafeString(l.getEntityName()));
                row.createCell(4).setCellValue(nullSafeString(l.getOldValue()));
                row.createCell(5).setCellValue(nullSafeString(l.getNewValue()));
                row.createCell(6).setCellValue(nullSafeString(l.getIpAddress()));
                row.createCell(7).setCellValue(nullSafeString(l.getUserAgent()));
                row.createCell(8).setCellValue(nullSafeString(l.getUrl()));
                row.createCell(9).setCellValue(nullSafeString(l.getMethod()));
                row.createCell(10).setCellValue(l.getTimestamp() != null ? l.getTimestamp().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel export", e);
        }
    }

    private String escapeCSV(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    private String nullSafeString(String v) { return v == null ? "" : v; }
    private String nullSafe(Object o) { return o == null ? "" : o.toString(); }
}

