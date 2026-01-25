
package com.example.common.exception;

import com.example.common.util.ResponseWrapper;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Global reusable exception handler
 *  - Handles JWT errors
 *  - Validation errors
 *  - Generic server errors
 *  - Can be reused across all microservices
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------------
    // 🔐 JWT / Authorization errors
    // ------------------------------------------------------------------------
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleJwtException(JwtException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", "Invalid or expired token");
        details.put("message", ex.getMessage());
        details.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, "Unauthorized", details));
    }

    // ------------------------------------------------------------------------
    // ⚠️ Validation errors (e.g. @Valid)
    // ------------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(new ResponseWrapper<>(false, "Validation failed", errors));
    }

    // ------------------------------------------------------------------------
    // ⚠️ Illegal arguments (e.g., invalid token header, null data, etc.)
    // ------------------------------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", "Bad Request");
        details.put("message", ex.getMessage());
        details.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .badRequest()
                .body(new ResponseWrapper<>(false, "Invalid request", details));
    }

    // ------------------------------------------------------------------------
    // 🔐 Authentication errors (No authenticated user)
    // ------------------------------------------------------------------------
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleRuntimeException(RuntimeException ex) {
        // Check if it's an authentication error
        if (ex.getMessage() != null && ex.getMessage().contains("No authenticated user")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseWrapper<>(false, "❌ Error: No authenticated user", null));
        }

        // For other runtime exceptions, fall through to generic handler
        return handleGenericException(ex);
    }

    // ------------------------------------------------------------------------
    // 💥 Catch-all fallback
    // ------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> handleGenericException(Exception ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", ex.getClass().getSimpleName());
        details.put("message", ex.getMessage());
        details.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseWrapper<>(false, "Internal Server Error", details));
    }
}

