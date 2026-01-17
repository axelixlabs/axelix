/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
