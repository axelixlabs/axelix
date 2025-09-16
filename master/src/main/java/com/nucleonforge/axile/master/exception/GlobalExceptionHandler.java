package com.nucleonforge.axile.master.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nucleonforge.axile.master.api.error.ApiError;
import com.nucleonforge.axile.master.api.error.handle.ApiExceptionTranslator;

/**
 * Global exception handler.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiExceptionTranslator apiExceptionTranslator;

    public GlobalExceptionHandler(ApiExceptionTranslator apiExceptionTranslator) {
        this.apiExceptionTranslator = apiExceptionTranslator;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEndpointException(Exception ex) {
        return apiExceptionTranslator.translateException(ex);
    }
}
