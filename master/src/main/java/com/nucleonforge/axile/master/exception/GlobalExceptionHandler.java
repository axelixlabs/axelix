package com.nucleonforge.axile.master.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;

/**
 * Global exception handler.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InstanceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEndpointException(InstanceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EndpointInvocationException.class)
    public ResponseEntity<Map<String, String>> handleEndpointException(EndpointInvocationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
