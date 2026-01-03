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

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nucleonforge.axelix.master.api.error.ApiError;
import com.nucleonforge.axelix.master.api.error.handle.ApiExceptionTranslator;

/**
 * The {@link OncePerRequestFilter} that is supposed to handle exceptions occurring
 * in the filter chain. All the other exceptions that propagate from the spring-web
 * endpoints will be already caught by {@link GlobalExceptionHandler}.
 *
 * @author Mikhail Polivakha
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ApiExceptionTranslator apiExceptionTranslator;
    private final ObjectMapper objectMapper;

    public ExceptionHandlingFilter(ApiExceptionTranslator apiExceptionTranslator, ObjectMapper objectMapper) {
        this.apiExceptionTranslator = apiExceptionTranslator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            doFilter(request, response, filterChain);
        } catch (ServletException e) {
            handleApiError(response, deriveApiError(e));
        } catch (IOException e) {
            handleApiError(response, apiExceptionTranslator.translateException(e));
        }
    }

    private ApiError deriveApiError(ServletException e) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof Exception rootCauseException) {
            return apiExceptionTranslator.translateException(rootCauseException);
        } else {
            return apiExceptionTranslator.translateException(e);
        }
    }

    private void handleApiError(HttpServletResponse response, ApiError apiError) throws IOException {
        response.setStatus(apiError.statusCode());
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
        response.getWriter().flush();
    }
}
