/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.master.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nucleonforge.axelix.master.api.error.ApiError;
import com.nucleonforge.axelix.master.api.error.handle.ApiExceptionTranslator;

/**
 * Global exception handler.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiExceptionTranslator apiExceptionTranslator;

    public GlobalExceptionHandler(ApiExceptionTranslator apiExceptionTranslator) {
        this.apiExceptionTranslator = apiExceptionTranslator;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEndpointException(Exception ex) {
        ApiError apiError = apiExceptionTranslator.translateException(ex);
        return ResponseEntity.status(apiError.statusCode()).body(apiError);
    }
}
