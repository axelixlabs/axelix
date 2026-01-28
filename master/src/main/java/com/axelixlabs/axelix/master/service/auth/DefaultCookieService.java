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

import org.springframework.http.ResponseCookie;

import com.axelixlabs.axelix.master.autoconfiguration.auth.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.JwtProperties;

/**
 * Default implementation of {@link CookieService}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
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
        return buildCookie(token, jwtProperties.getLifespan().toSeconds());
    }

    @Override
    public ResponseCookie buildExpiredAuthCookie() {
        // The browser will expire the cookie anyway, so empty string is fine
        return buildCookie("", 0L);
    }

    /**
     * zero in {@code cookieLifetimeInSeconds} means cookie will expire immediately.
     */
    private ResponseCookie buildCookie(String token, long cookieLifetimeInSeconds) {
        return ResponseCookie.from(cookieProperties.getName(), token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(cookieLifetimeInSeconds)
                .sameSite("Strict")
                .build();
    }
}
