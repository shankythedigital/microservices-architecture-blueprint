package com.example.notification.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> bad(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> locked(LockedException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<String> expired(CredentialsExpiredException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> disabled(DisabledException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> other(Exception ex) {
        return ResponseEntity.status(500).body("Internal error: " + ex.getMessage());
    }
}
