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
package com.axelixlabs.axelix.master.filter.auth;

import java.io.IOException;

import com.axelixlabs.axelix.master.filter.FiltersOrder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * Auth filter that is based on the {@link org.springframework.http.HttpHeaders#AUTHORIZATION} header.
 *
 * @author Nikita Kirilllov
 */
@Component
@Order(FiltersOrder.SELF_REGISTRATION_JWT_AUTHORIZATION_FILTER)
public class SelfRegistrationJwtAuthorizationFilter extends OncePerRequestFilter {

    private final WebIdentityAccessManager webIdentityAccessManager;
    private final SecurityContextExecutor securityContextExecutor;

    public SelfRegistrationJwtAuthorizationFilter(
            WebIdentityAccessManager webIdentityAccessManager, SecurityContextExecutor securityContextExecutor) {
        this.webIdentityAccessManager = webIdentityAccessManager;
        this.securityContextExecutor = securityContextExecutor;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !path.equalsIgnoreCase("/api/internal/service/register");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null || token.isBlank()) {
            throw new JwtProcessingException("Authorization token is missing");
        }

        User user = webIdentityAccessManager.verifyAccess(
                request.getServletPath(), HttpMethod.valueOf(request.getMethod()), token);

        try {
            securityContextExecutor.runWithinSecurityContext(
                    () -> filterChain.doFilter(request, response), new DefaultSecurityContext(user, token));
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Nullable
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String bearerScheme = AuthenticationSchemes.BEARER.prefix();
        if (header != null) {
            if (header.startsWith(bearerScheme)) {
                return header.substring(bearerScheme.length());
            }
        }
        return null;
    }
}
