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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.master.api.infrastructure.InfrastructureApiPaths;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.filter.FiltersOrder;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.ExternalWebRequestContext;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.MasterRequestContextInitFilter;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebSuccessfulResult;

/**
 * Auth filter that is based on the {@link org.springframework.http.HttpHeaders#SET_COOKIE Set-Cookie} header.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Order(FiltersOrder.EXTERNAL_API_JWT_AUTHORIZATION_FILTER)
public class ExternalApiCookieAuthorizationFilter extends OncePerRequestFilter {

    private final SecurityContextExecutor securityContextExecutor;
    private final JwtDecoderService jwtDecoderService;
    private final Authorizer authorizer;
    private final List<OnWebSuccessfulResult> onSuccessInterceptors;

    public ExternalApiCookieAuthorizationFilter(
            SecurityContextExecutor securityContextExecutor,
            JwtDecoderService jwtDecoderService,
            Authorizer authorizer,
            List<OnWebSuccessfulResult> interceptors) {

        this.securityContextExecutor = securityContextExecutor;
        this.jwtDecoderService = jwtDecoderService;
        this.authorizer = authorizer;
        this.onSuccessInterceptors = interceptors;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Static content (/, /index.html, /assets/*, etc.) is served at root and does not require auth
        // as well as actuator health endpoints
        // TODO: We must refactor it
        return !path.startsWith("/api/")
                || path.startsWith(InfrastructureApiPaths.HEALTH_STATUS_PATH)
                || path.startsWith(InfrastructureApiPaths.PROMETHEUS_METRICS_SCRAPE_PATH)
                || path.equalsIgnoreCase("/api/external/users/login")
                || path.startsWith("/api/external/oauth2/callback")
                || path.startsWith("/api/external/settings")
                || path.equalsIgnoreCase("/api/internal/service/register")
                || path.startsWith("/api/mcp")
                || path.equalsIgnoreCase("/api/external/mcp-oauth2/.well-known/oauth-protected-resource");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        ExternalWebRequestContext currentRequestContext = MasterRequestContextInitFilter.requireWebRequestContext();

        String token = resolveToken(request.getCookies());

        if (token == null || token.isBlank()) {
            throw new JwtProcessingException("Authorization token is missing");
        }

        try {
            PasswordlessUser user = jwtDecoderService.decodeTokenToUser(token);

            authorizeUser(currentRequestContext, user);

            securityContextExecutor.runWithinSecurityContext(
                    () -> {
                        filterChain.doFilter(request, response);
                        onSuccessfulResult(request, currentRequestContext, user);
                    },
                    new DefaultSecurityContext(user, token));
        } catch (JwtProcessingException | ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void authorizeUser(ExternalWebRequestContext currentRequestContext, PasswordlessUser decodedTokenToUser) {
        Set<Authority> requiredAuthorities = Optional.ofNullable(
                        currentRequestContext.masterWebEndpoint().authority())
                .map(Set::of)
                .orElse(Collections.emptySet());

        AuthorizationRequest authorizationRequest = new AuthorizationRequest(requiredAuthorities);

        authorizer.authorize(decodedTokenToUser, authorizationRequest);
    }

    @Nullable
    private String resolveToken(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CookieProperties.AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void onSuccessfulResult(
            HttpServletRequest request, ExternalWebRequestContext currentRequestContext, User user) {

        onSuccessInterceptors.forEach(it -> it.onSuccess(currentRequestContext.masterWebEndpoint(), request, user));
    }
}
