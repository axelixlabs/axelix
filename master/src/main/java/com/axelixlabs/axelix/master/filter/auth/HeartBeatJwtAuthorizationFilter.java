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
import java.util.Set;

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
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.InternalAuthorities;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.master.api.internal.ApiPaths;
import com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfiguration;
import com.axelixlabs.axelix.master.filter.FiltersOrder;

/**
 * Auth filter that is supposed to act like a central point for authenticating and authorizing the heartbeats.
 *
 * @author Nikita Kirilllov
 * @author Mikhail Polivakha
 */
@Component
@Order(FiltersOrder.HEART_BEAT_JWT_AUTHORIZATION_FILTER)
public class HeartBeatJwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String HEART_BEAT_REQUEST_PATH =
            WebAutoConfiguration.INTERNAL_API_PATH + ApiPaths.HeartBeatApi.SERVICE_REGISTER;

    private final SecurityContextExecutor securityContextExecutor;
    private final JwtDecoderService jwtDecoderService;
    private final Authorizer authorizer;

    public HeartBeatJwtAuthorizationFilter(
            SecurityContextExecutor securityContextExecutor,
            JwtDecoderService jwtDecoderService,
            Authorizer authorizer) {
        this.securityContextExecutor = securityContextExecutor;
        this.jwtDecoderService = jwtDecoderService;
        this.authorizer = authorizer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !path.equalsIgnoreCase(HEART_BEAT_REQUEST_PATH);
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

        PasswordlessUser user = jwtDecoderService.decodeTokenToUser(token);

        AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(Set.of(InternalAuthorities.HEART_BEATING_AUTHORITY));

        authorizer.authorize(user, authorizationRequest);

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
