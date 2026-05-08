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
package com.axelixlabs.axelix.master.filter;

import java.io.IOException;

import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiExceptionTranslator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The {@link OncePerRequestFilter} that is supposed to handle exceptions
 * during processing HTTP requests.
 *
 * @author Mikhail Polivakha
 */
@Order(FiltersOrder.EXCEPTION_HANDLING_FILTER)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ApiExceptionTranslator apiExceptionTranslator;

    private static final String ERROR_RESPONSE_TEMPLATE =
            // langauge=json
            """
        {
            "errorCode" : "%s"
        }
        """;

    public ExceptionHandlingFilter(ApiExceptionTranslator apiExceptionTranslator) {
        this.apiExceptionTranslator = apiExceptionTranslator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException e) {
            handleApiError(response, deriveApiError(e));
        } catch (Exception e) {
            handleApiError(response, apiExceptionTranslator.translateException(e));
        }
    }

    /**
     * This code needs clarification. We need to process {@link ServletException} separately since
     * Jakarta Servlet Specification requires that in case of any unrecognized/unkwon exception,
     * the Servlet Container MUST wrap it up in the {@link ServletException}. So we have to unwrap it if
     * necessary.
     */
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
        response.getWriter().write(ERROR_RESPONSE_TEMPLATE.formatted(apiError.errorCode()));
        response.getWriter().flush();
    }
}
