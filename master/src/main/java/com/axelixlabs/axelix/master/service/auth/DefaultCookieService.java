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
package com.axelixlabs.axelix.master.service.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseCookie;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;

/**
 * Default implementation of {@link CookieService}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultCookieService implements CookieService {

    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public DefaultCookieService(CookieProperties cookieProperties, JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public ResponseCookie buildAuthCookie(String token) {
        return buildAuthCookie(token, jwtProperties.lifespan().toSeconds());
    }

    @Override
    public ResponseCookie buildAuthoritiesMetadataCookie(Set<Role> roles) {
        String authoritiesJsonArray = roles.stream()
                .flatMap(role -> role.getAuthorities().stream())
                .map(Authority::getName)
                .distinct()
                .collect(Collectors.joining(",", "[", "]"));

        String base64EncodedAuthoritiesMetadata = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(authoritiesJsonArray.getBytes(StandardCharsets.UTF_8));

        return buildAuthoritiesMetadataCookie(
                base64EncodedAuthoritiesMetadata, jwtProperties.lifespan().getSeconds());
    }

    @Override
    public ResponseCookie buildExpiredAuthCookie() {
        // The browser will expire the cookie anyway, so empty string is fine
        return buildAuthCookie("", 0L);
    }

    @Override
    public ResponseCookie buildExpiredAuthMetadataCookie() {
        // The browser will expire the cookie anyway, so empty string is fine
        return buildAuthoritiesMetadataCookie("", 0L);
    }

    /**
     * zero in {@code cookieLifetimeInSeconds} means cookie will expire immediately.
     */
    private ResponseCookie buildAuthCookie(String token, long cookieLifetimeInSeconds) {
        return ResponseCookie.from(cookieProperties.getAuthCookieName(), token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(cookieLifetimeInSeconds)
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie buildAuthoritiesMetadataCookie(String authoritiesJsonArray, long cookieLifetimeInSeconds) {
        return ResponseCookie.from(cookieProperties.getAuthoritiesCookieName(), authoritiesJsonArray)
                .httpOnly(false)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(cookieLifetimeInSeconds)
                .sameSite("Strict")
                .build();
    }
}
