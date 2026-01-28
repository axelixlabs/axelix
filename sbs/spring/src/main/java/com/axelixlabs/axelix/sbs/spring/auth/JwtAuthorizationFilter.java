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
package com.axelixlabs.axelix.sbs.spring.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DecodedUser;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;

/**
 * A custom servlet filter that restricts access to Actuator endpoints based on JWT token presence, validity,
 * and mapped {@link Authority} authorities.
 * <p>
 * Rejects unauthorized requests before they reach the application logic.
 *
 * @author Nikita Kirillov
 * @since 29.07.2025
 */
@SuppressWarnings("NullAway") // TODO: Pending issue GH-42 – introduce exception translator and refactor this filter
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtDecoderService jwtDecoderService;

    private final AuthorityResolver defaultAuthorityResolver;

    private final Authorizer authorizer;

    public JwtAuthorizationFilter(
            JwtDecoderService jwtDecoderService, AuthorityResolver defaultAuthorityResolver, Authorizer authorizer) {
        this.jwtDecoderService = jwtDecoderService;
        this.defaultAuthorityResolver = defaultAuthorityResolver;
        this.authorizer = authorizer;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is missing");
            return;
        }

        String requestPath = request.getRequestURI();

        try {
            DecodedUser user = jwtDecoderService.decodeTokenToUser(token);
            Optional<Authority> requiredAuthority = defaultAuthorityResolver.resolve(requestPath);

            AuthorizationRequest authorizationRequest =
                    new AuthorizationRequest(requiredAuthority.map(Set::of).orElse(Collections.emptySet()));

            authorizer.authorize(user, authorizationRequest);

            filterChain.doFilter(request, response);

        } catch (JwtParsingException | ExpiredJwtTokenException | InvalidJwtTokenException e) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (AuthorizationException e) {
            respondWith(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    @Nullable
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void respondWith(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
