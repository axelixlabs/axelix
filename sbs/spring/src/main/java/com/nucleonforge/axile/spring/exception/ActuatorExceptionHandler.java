package com.nucleonforge.axile.spring.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nucleonforge.axile.spring.properties.PropertyNameIsNotValidException;
import com.nucleonforge.axile.spring.properties.PropertyNotFoundException;

/**
 * Global exception handler for Spring Boot Actuator endpoints.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
@RestControllerAdvice
public class ActuatorExceptionHandler {

    @ExceptionHandler({PropertyNotFoundException.class, PropertyNameIsNotValidException.class})
    public ResponseEntity<Map<String, String>> handleEndpointException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
