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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultCookieService}.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
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
        // given.
        String testToken = "test-jwt-token-123";

        // when.
        ResponseCookie cookie = cookieService.buildAuthCookie(testToken);

        // then.
        assertThat(cookie).isNotNull().satisfies(c -> {
            assertThat(c.getValue()).isEqualTo(testToken);
            assertThat(c.getName()).isEqualTo(cookieProperties.getName());
            assertThat(c.isHttpOnly()).isTrue();
            assertThat(c.getPath()).isEqualTo("/");
            assertThat(c.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(c.getMaxAge()).isEqualTo(jwtProperties.getLifespan());
        });
    }

    @Test
    void buildAuthoritiesMetadataCookie_WithDistinctAuthorities_ReturnsBase64EncodedJsonCookie() {
        // given.
        Role targetRole =
                new DefaultRole("OBSERVER", Set.of(DefaultAuthority.CACHES_TOGGLE, DefaultAuthority.ENV_VALUES_READ));

        // when.
        ResponseCookie cookie = cookieService.buildAuthoritiesMetadataCookie(Set.of(targetRole));
        String decodedValue = new String(Base64.getDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);

        // then.
        assertThat(cookie).isNotNull().satisfies(c -> {
            assertThat(c.getName()).isEqualTo(cookieProperties.getNameAuthority());
            assertThat(c.isHttpOnly()).isFalse();
            assertThat(c.getPath()).isEqualTo("/");
            assertThat(c.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(c.getMaxAge()).isEqualTo(jwtProperties.getLifespan());
        });
        assertThat(decodedValue).startsWith("[").endsWith("]");
        assertThat(decodedValue).contains(DefaultAuthority.CACHES_TOGGLE.name());
        assertThat(decodedValue).contains(DefaultAuthority.ENV_VALUES_READ.name());
    }

    @Test
    void buildAuthoritiesMetadataCookie_WithNoAuthorities_ReturnsBase64EncodedJsonEmptyArrayCookie() {
        // given.
        Role targetRole = new DefaultRole("VIEWER", Set.of());

        // when.
        ResponseCookie cookie = cookieService.buildAuthoritiesMetadataCookie(Set.of(targetRole));
        String decodedValue = new String(Base64.getDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);

        // then.
        assertThat(cookie).isNotNull().satisfies(c -> {
            assertThat(c.getName()).isEqualTo(cookieProperties.getNameAuthority());
            assertThat(c.isHttpOnly()).isFalse();
            assertThat(c.getPath()).isEqualTo("/");
            assertThat(c.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(c.getMaxAge()).isEqualTo(jwtProperties.getLifespan());
        });
        assertThat(decodedValue).isEqualTo("[]");
    }

    @Test
    void buildExpiredAuthCookie_ReturnsExpiredCookieWithEmptyValue() {
        // when.
        ResponseCookie cookie = cookieService.buildExpiredAuthCookie();

        // then.
        assertThat(cookie.getName()).isEqualTo(cookieProperties.getName());
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().toSeconds()).isZero();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    void buildExpiredAuthMetadataCookie_ReturnsExpiredCookieWithEmptyValue() {
        // when.
        ResponseCookie cookie = cookieService.buildExpiredAuthMetadataCookie();

        // then.
        assertThat(cookie.getName()).isEqualTo(cookieProperties.getNameAuthority());
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().toSeconds()).isZero();
        assertThat(cookie.isHttpOnly()).isFalse();
    }
}
