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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.autoconfiguration.auth.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.JwtProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultCookieService}.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@TestPropertySource(
        properties = {"axelix.master.auth.cookie.domain=example.com", "axelix.master.auth.jwt.lifespan=12h"})
class DefaultCookieServiceTest {

    @Autowired
    private CookieService cookieService;

    @Autowired
    private CookieProperties cookieProperties;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void buildAuthCookie_WithAllProperties_ReturnsCorrectlyConfiguredCookie() {
        String testToken = "test-jwt-token-123";

        ResponseCookie cookie = cookieService.buildAuthCookie(testToken);

        assertThat(cookie).isNotNull().satisfies(c -> {
            assertThat(c.getValue()).isEqualTo(testToken);
            assertThat(c.getName()).isEqualTo(cookieProperties.getName());
            assertThat(c.isHttpOnly()).isTrue();
            assertThat(c.getPath()).isEqualTo("/");
            assertThat(c.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(c.getMaxAge()).isEqualTo(jwtProperties.getLifespan());
        });
    }
}
