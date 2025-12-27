package com.example.asset.exception;

import com.example.common.util.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ✅ GlobalComplianceExceptionHandler
 * Global exception handler for compliance-related exceptions.
 */
@RestControllerAdvice
public class GlobalComplianceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalComplianceExceptionHandler.class);

    @ExceptionHandler(ComplianceException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleComplianceException(ComplianceException e) {
        log.error("❌ Compliance violation: {}", e.getMessage());
        
        HttpStatus status = e.isBlocking() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        
        return ResponseEntity.status(status)
                .body(new ResponseWrapper<>(
                        false,
                        e.getMessage(),
                        e.getViolations()));
    }
}
