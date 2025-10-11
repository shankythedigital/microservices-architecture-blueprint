package com.example.asset.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> badCred(BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Invalid credentials: " + ex.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> locked(LockedException ex) {
        return ResponseEntity.status(423).body("Account locked: " + ex.getMessage());
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<String> expired(CredentialsExpiredException ex) {
        return ResponseEntity.status(401).body("Credentials expired: " + ex.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> disabled(DisabledException ex) {
        return ResponseEntity.status(403).body("Account disabled: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generic(Exception ex) {
        return ResponseEntity.internalServerError().body("Unexpected error: " + ex.getMessage());
    }
}
