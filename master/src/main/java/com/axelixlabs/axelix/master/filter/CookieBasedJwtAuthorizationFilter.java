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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;

/**
 * Auth filter that is based on the {@link org.springframework.http.HttpHeaders#SET_COOKIE Set-Cookie} header.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
public class CookieBasedJwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtDecoderService jwtDecoderService;
    private final String authCookieName;

    public CookieBasedJwtAuthorizationFilter(JwtDecoderService jwtDecoderService, String authCookieName) {
        this.jwtDecoderService = jwtDecoderService;
        this.authCookieName = authCookieName;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Static content (/, /index.html, /assets/*, etc.) is served at root and does not require auth
        // as well as actuator health endpoints
        return !path.startsWith("/api/")
                || path.startsWith("/api/actuator/health")
                // Temporarily excluded /api/mcp, waiting for the mcp server authentication issue to be resolved
                // https://github.com/axelixlabs/axelix/issues/758
                || path.startsWith("/api/mcp")
                || path.equalsIgnoreCase("/api/external/users/login");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request.getCookies());

        if (token == null || token.isBlank()) {
            throw new ServletException(new JwtProcessingException("Authorization token is missing"));
        }

        try {
            jwtDecoderService.decodeTokenToUser(token);

            filterChain.doFilter(request, response);

        } catch (JwtParsingException | ExpiredJwtTokenException | InvalidJwtTokenException e) {
            throw new ServletException(e);
        }
    }

    @Nullable
    private String resolveToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (authCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
