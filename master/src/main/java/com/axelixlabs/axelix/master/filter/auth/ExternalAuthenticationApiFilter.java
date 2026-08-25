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
import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.filter.FiltersOrder;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.ExternalWebRequestContext;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.MasterRequestContextInitFilter;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebSuccessfulResult;

/**
 * Filter that is supposed to intercept the authentication-related external master apis.
 *
 * @see ExternalApiCookieAuthorizationFilter
 *
 * @author Mikhail Polivakha
 */
@NullMarked
@Order(FiltersOrder.EXTERNAL_AUTHENTICATION_API_FILTER)
public class ExternalAuthenticationApiFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthenticationApiFilter.class);

    private final JwtDecoderService jwtDecoderService;
    private final List<OnWebSuccessfulResult> onSuccessInterceptors;

    public ExternalAuthenticationApiFilter(
            JwtDecoderService jwtDecoderService, List<OnWebSuccessfulResult> onSuccessInterceptors) {
        this.jwtDecoderService = jwtDecoderService;
        this.onSuccessInterceptors = onSuccessInterceptors;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !shouldFilter(request);
    }

    private static boolean shouldFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.equalsIgnoreCase("/api/external/users/login") || path.startsWith("/api/external/oauth2/callback");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ExternalWebRequestContext currentRequestContext = MasterRequestContextInitFilter.requireWebRequestContext();

        filterChain.doFilter(request, response);

        Optional<HttpCookie> authCookie = findAuthCookie(response);

        authCookie.ifPresentOrElse(
                httpCookie -> {
                    PasswordlessUser decodedTokenToUser = jwtDecoderService.decodeTokenToUser(httpCookie.getValue());
                    onSuccessfulResult(request, currentRequestContext, decodedTokenToUser);
                },
                ExternalAuthenticationApiFilter::warning);
    }

    private static void warning() {
        logger.warn(
                "{} cookie was not found in the response to the login-like endpoint. "
                        + "That is unexpected. Please, consider reporting that to maintainers",
                CookieProperties.AUTH_COOKIE_NAME);
    }

    private static Optional<HttpCookie> findAuthCookie(HttpServletResponse response) {
        String header = response.getHeader(HttpHeaders.SET_COOKIE);

        List<HttpCookie> cookie = HttpCookie.parse(header);

        return cookie.stream()
                .filter(httpCookie -> CookieProperties.AUTH_COOKIE_NAME.equalsIgnoreCase(httpCookie.getName()))
                .findFirst();
    }

    private void onSuccessfulResult(
            HttpServletRequest request, ExternalWebRequestContext currentRequestContext, User user) {

        onSuccessInterceptors.forEach(it -> it.onSuccess(currentRequestContext.masterWebEndpoint(), request, user));
    }
}
