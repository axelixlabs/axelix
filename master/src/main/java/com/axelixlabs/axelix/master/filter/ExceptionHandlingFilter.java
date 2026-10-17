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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiExceptionTranslator;
import com.axelixlabs.axelix.master.service.auth.CookieService;

/**
 * The {@link OncePerRequestFilter} that is supposed to handle exceptions
 * during processing HTTP requests.
 *
 * @author Mikhail Polivakha
 */
@Order(FiltersOrder.EXCEPTION_HANDLING_FILTER)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ApiExceptionTranslator apiExceptionTranslator;
    private final JsonMapper jsonMapper;
    private final CookieService cookieService;

    public ExceptionHandlingFilter(
            ApiExceptionTranslator apiExceptionTranslator, JsonMapper jsonMapper, CookieService cookieService) {
        this.apiExceptionTranslator = apiExceptionTranslator;
        this.jsonMapper = jsonMapper;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException e) {
            handleApiError(response, deriveApiError(request, e));
        } catch (Exception e) {
            handleApiError(response, apiExceptionTranslator.translateException(request, e));
        }
    }

    /**
     * This code needs clarification. We need to process {@link ServletException} separately since
     * Jakarta Servlet Specification requires that in case of any unrecognized/unkwon exception,
     * the Servlet Container MUST wrap it up in the {@link ServletException}. So we have to unwrap it if
     * necessary.
     */
    private ApiError deriveApiError(HttpServletRequest request, ServletException e) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof Exception rootCauseException) {
            return apiExceptionTranslator.translateException(request, rootCauseException);
        } else {
            return apiExceptionTranslator.translateException(request, e);
        }
    }

    private void handleApiError(HttpServletResponse response, ApiError apiError) throws IOException {
        response.setStatus(apiError.statusCode());

        if (apiError.statusCode() == HttpStatus.UNAUTHORIZED.value()) {
            expireAuthCookies(response);
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonMapper.writeValueAsString(apiError.errorBody()));
        response.getWriter().flush();
    }

    /**
     * The UI considers the user authenticated as long as the authorities cookie is present, so
     * on 401 we have to expire it along with the auth cookie, otherwise the UI would never get
     * to the login page.
     */
    private void expireAuthCookies(HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE, cookieService.buildExpiredAuthCookie().toString());
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieService.buildExpiredAuthMetadataCookie().toString());
    }
}
