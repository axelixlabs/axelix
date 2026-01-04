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
package com.nucleonforge.axelix.sbs.spring.auth;

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

import com.nucleonforge.axelix.common.auth.JwtDecoderService;
import com.nucleonforge.axelix.common.auth.core.Authority;
import com.nucleonforge.axelix.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axelix.common.auth.core.DecodedUser;
import com.nucleonforge.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.nucleonforge.axelix.common.auth.exception.InvalidJwtTokenException;
import com.nucleonforge.axelix.common.auth.exception.JwtParsingException;

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
