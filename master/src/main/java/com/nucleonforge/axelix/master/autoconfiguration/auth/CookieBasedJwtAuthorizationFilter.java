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
package com.nucleonforge.axelix.master.autoconfiguration.auth;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nucleonforge.axelix.common.auth.JwtDecoderService;
import com.nucleonforge.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axelix.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axelix.common.auth.exception.JwtParsingException;
import com.nucleonforge.axelix.common.auth.exception.JwtTokenDecodingException;

/**
 * Auth filter that is based on the {@link org.springframework.http.HttpHeaders#SET_COOKIE Set-Cookie} header.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway") // TODO: Pending issue GH-42 – introduce exception translator and refactor this filter
public class CookieBasedJwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtDecoderService jwtDecoderService;
    private final String authCookie;

    public CookieBasedJwtAuthorizationFilter(
            JwtDecoderService jwtDecoderService, @Value("${axile.master.auth.cookie.name}") String authCookie) {
        this.jwtDecoderService = jwtDecoderService;
        this.authCookie = authCookie;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // TODO:
        //  Here, the login endpoint URL is hardcoded, which is not really a good thing to be doing
        return path.equals("/api/axelix/users/login");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request.getCookies());

        if (token == null || token.isBlank()) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is missing");
            return;
        }

        try {
            jwtDecoderService.decodeTokenToUser(token);

            filterChain.doFilter(request, response);

        } catch (JwtParsingException | ExpiredJwtTokenException | InvalidJwtTokenException e) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (JwtTokenDecodingException e) {
            respondWith(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Nullable
    private String resolveToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (authCookie.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void respondWith(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
