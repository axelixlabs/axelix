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
import org.springframework.data.util.ProxyUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.master.filter.FiltersOrder;
import com.axelixlabs.axelix.master.service.auth.intercept.IamEvaluationInterceptor;
import com.axelixlabs.axelix.master.service.auth.intercept.OnAccessDenied;
import com.axelixlabs.axelix.master.service.auth.intercept.OnInvalidTokenInRequest;

/**
 * Auth filter that is based on the {@link org.springframework.http.HttpHeaders#SET_COOKIE Set-Cookie} header.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Order(FiltersOrder.COOKIE_BASED_JWT_AUTHORIZATION_FILTER)
public class CookieBasedJwtAuthorizationFilter extends OncePerRequestFilter {

    private final String authCookieName;
    private final SecurityContextExecutor securityContextExecutor;
    private final JwtDecoderService jwtDecoderService;
    private final Authorizer authorizer;
    private final List<OnInvalidTokenInRequest> onInvalidTokenInRequestInterceptors;
    private final List<OnAccessDenied> onAccessDeniedInterceptors;

    public CookieBasedJwtAuthorizationFilter(
            String authCookieName,
            SecurityContextExecutor securityContextExecutor,
            JwtDecoderService jwtDecoderService,
            Authorizer authorizer,
            List<IamEvaluationInterceptor> interceptors) {
        this.authCookieName = authCookieName;
        this.securityContextExecutor = securityContextExecutor;
        this.jwtDecoderService = jwtDecoderService;
        this.authorizer = authorizer;
        this.onInvalidTokenInRequestInterceptors = getInterceptorsOfType(interceptors, OnInvalidTokenInRequest.class);
        this.onAccessDeniedInterceptors = getInterceptorsOfType(interceptors, OnAccessDenied.class);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Static content (/, /index.html, /assets/*, etc.) is served at root and does not require auth
        // as well as actuator health endpoints
        // TODO: We must refactor it
        return !path.startsWith("/api/")
                || path.startsWith("/api/actuator/health")
                || path.startsWith("/api/actuator/prometheus")
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

        WebRequestContext currentRequestContext = WebRequestContextInitFilter.getCurrentRequestContext();

        String token = resolveToken(request.getCookies());

        if (token == null || token.isBlank()) {
            onInvalidTokenCallback(request, currentRequestContext);
            throw new JwtProcessingException("Authorization token is missing");
        }

        try {
            PasswordlessUser user = jwtDecoderService.decodeTokenToUser(token);

            authorizeUser(request, currentRequestContext, user);

            securityContextExecutor.runWithinSecurityContext(
                    () -> filterChain.doFilter(request, response), new DefaultSecurityContext(user, token));
        } catch (AuthorizationException e) {
            onInvalidTokenCallback(request, currentRequestContext);
            throw e;
        } catch (ServletException | IOException e) {
            // TODO: What do we do when the user encounters a general error?
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void authorizeUser(
            HttpServletRequest request, WebRequestContext currentRequestContext, PasswordlessUser decodedTokenToUser) {
        try {
            Set<Authority> requiredAuthorities = Optional.ofNullable(
                            currentRequestContext.masterWebEndpoint().authority())
                    .map(Set::of)
                    .orElse(Collections.emptySet());

            AuthorizationRequest authorizationRequest = new AuthorizationRequest(requiredAuthorities);

            authorizer.authorize(decodedTokenToUser, authorizationRequest);
        } catch (AuthorizationException e) {
            onAccessDenied(request, currentRequestContext, decodedTokenToUser);
            throw e;
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

    private void onInvalidTokenCallback(HttpServletRequest request, WebRequestContext currentRequestContext) {

        onInvalidTokenInRequestInterceptors.forEach(
                it -> it.onRequest(currentRequestContext.masterWebEndpoint(), request));
    }

    private void onAccessDenied(HttpServletRequest request, WebRequestContext currentRequestContext, User user) {

        onAccessDeniedInterceptors.forEach(
                it -> it.onRequest(currentRequestContext.masterWebEndpoint(), request, user));
    }

    private static <T> List<T> getInterceptorsOfType(
            List<IamEvaluationInterceptor> interceptors, Class<T> interceptorType) {
        return interceptors.stream()
                .filter(it -> interceptorType.isAssignableFrom(ProxyUtils.getUserClass(it.getClass())))
                .map(interceptorType::cast)
                .toList();
    }
}
