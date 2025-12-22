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
package com.nucleonforge.axile.common.auth.basic.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nucleonforge.axile.common.auth.basic.jwt.service.BasicJwtDecoderService;
import com.nucleonforge.axile.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.common.auth.exception.JwtTokenDecodingException;

/**
 * A custom servlet filter that restricts access to Actuator endpoints based on valid JWT token presence.
 *
 * @author Nikita Kirillov
 * @since 29.07.2025
 */
@SuppressWarnings("NullAway")
public class BasicJwtAuthorizationFilter extends OncePerRequestFilter {

    private final BasicJwtDecoderService basicJwtDecoderService;

    public BasicJwtAuthorizationFilter(BasicJwtDecoderService basicJwtDecoderService) {
        this.basicJwtDecoderService = basicJwtDecoderService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader(HttpHeaders.SET_COOKIE);

        if (token == null || token.isBlank()) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is missing");
            return;
        }

        try {
            basicJwtDecoderService.isValidToken(token);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtTokenException | InvalidJwtTokenException e) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (JwtTokenDecodingException e) {
            respondWith(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void respondWith(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
