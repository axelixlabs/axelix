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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.CookieProperties;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.JwtProperties;
import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.DefaultAuthority;
import com.axelixlabs.axelix.master.domain.auth.DefaultRole;
import com.axelixlabs.axelix.master.domain.auth.Role;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultCookieService}.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest
@TestPropertySource(properties = {"axelix.master.auth.jwt.lifespan=12h"})
class DefaultCookieServiceTest {
    private Authority envValuesRead;
    private Authority cachesClear;
    private Authority cachesToggle;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void insertAuthorities() {
        envValuesRead = insertAuthority("test-ENV_VALUES_READ", "test:environment.values:read");
        cachesClear = insertAuthority("test-CACHES_CLEAR", "test:caches:clear");
        cachesToggle = insertAuthority("test-CACHES_TOGGLE", "test:caches:toggle");
    }

    @AfterEach
    void deleteAuthorities() {
        jdbcClient
                .sql(
                        "DELETE FROM authorities WHERE id IN ('test-ENV_VALUES_READ', 'test-CACHES_CLEAR', 'test-CACHES_TOGGLE')")
                .update();
    }

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
        ResponseCookie authCookie = cookieService.buildAuthCookie(testToken);

        // then.
        assertThat(authCookie).isNotNull().satisfies(cookie -> {
            assertThat(cookie.getValue()).isEqualTo(testToken);
            assertThat(cookie.getName()).isEqualTo(CookieProperties.AUTH_COOKIE_NAME);
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(cookie.getMaxAge()).isEqualTo(jwtProperties.lifespan());
        });
    }

    @Test
    void buildAuthoritiesMetadataCookie_WithDistinctAuthorities_ReturnsBase64EncodedJsonCookie() {
        // given.
        Role targetRole = new DefaultRole("OBSERVER", Set.of(cachesToggle, envValuesRead));

        // when.
        ResponseCookie authoritiesCookie = cookieService.buildAuthoritiesMetadataCookie(Set.of(targetRole));
        String decodedValue =
                new String(Base64.getDecoder().decode(authoritiesCookie.getValue()), StandardCharsets.UTF_8);

        // then.
        assertThat(authoritiesCookie).isNotNull().satisfies(cookie -> {
            assertThat(cookie.getName()).isEqualTo(CookieProperties.AUTHORITIES_COOKIE_NAME);
            assertThat(cookie.isHttpOnly()).isFalse();
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(cookie.getMaxAge()).isEqualTo(jwtProperties.lifespan());
        });
        assertThat(decodedValue).startsWith("[").endsWith("]");
        assertThat(decodedValue).contains("test:caches:toggle");
        assertThat(decodedValue).contains("test:environment.values:read");
    }

    @Test
    void buildAuthoritiesMetadataCookie_WithInheritedAuthorities_IncludesTheOnesReachedThroughTheComponents() {
        // given. The front-end decides what to show from this cookie, so an inherited authority has to reach it just
        // like a direct one
        Role grandParent = new DefaultRole("GRAND_PARENT", Set.of(cachesClear));
        Role child = new DefaultRole(
                "CHILD",
                Set.of(cachesToggle),
                Set.of(new DefaultRole("PARENT", Set.of(envValuesRead), Set.of(grandParent))));

        // when.
        ResponseCookie authoritiesCookie = cookieService.buildAuthoritiesMetadataCookie(Set.of(child));
        String decodedValue =
                new String(Base64.getDecoder().decode(authoritiesCookie.getValue()), StandardCharsets.UTF_8);

        // then. Own, inherited and inherited-transitively alike
        assertThat(decodedValue)
                .contains("test:caches:toggle")
                .contains("test:environment.values:read")
                .contains("test:caches:clear");
    }

    @Test
    void buildAuthoritiesMetadataCookie_WithNoAuthorities_ReturnsBase64EncodedJsonEmptyArrayCookie() {
        // given.
        Role targetRole = new DefaultRole("EMPTY_TEST_ROLE", Set.of());

        // when.
        ResponseCookie authoritiesCookie = cookieService.buildAuthoritiesMetadataCookie(Set.of(targetRole));
        String decodedValue =
                new String(Base64.getDecoder().decode(authoritiesCookie.getValue()), StandardCharsets.UTF_8);

        // then.
        assertThat(authoritiesCookie).isNotNull().satisfies(cookie -> {
            assertThat(cookie.getName()).isEqualTo(CookieProperties.AUTHORITIES_COOKIE_NAME);
            assertThat(cookie.isHttpOnly()).isFalse();
            assertThat(cookie.getPath()).isEqualTo("/");
            assertThat(cookie.isSecure()).isEqualTo(cookieProperties.isSecure());
            assertThat(cookie.getMaxAge()).isEqualTo(jwtProperties.lifespan());
        });
        assertThat(decodedValue).isEqualTo("[]");
    }

    @Test
    void buildExpiredAuthCookie_ReturnsExpiredCookieWithEmptyValue() {
        // when.
        ResponseCookie cookie = cookieService.buildExpiredAuthCookie();

        // then.
        assertThat(cookie.getName()).isEqualTo(CookieProperties.AUTH_COOKIE_NAME);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().toSeconds()).isZero();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    void buildExpiredAuthMetadataCookie_ReturnsExpiredCookieWithEmptyValue() {
        // when.
        ResponseCookie cookie = cookieService.buildExpiredAuthMetadataCookie();

        // then.
        assertThat(cookie.getName()).isEqualTo(CookieProperties.AUTHORITIES_COOKIE_NAME);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().toSeconds()).isZero();
        assertThat(cookie.isHttpOnly()).isFalse();
    }

    private Authority insertAuthority(String id, String codename) {
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params(id, codename, "Test authority")
                .update();
        return jdbcClient
                .sql("SELECT id, codename FROM authorities WHERE id = ?")
                .param(id)
                .query((rs, n) -> new DefaultAuthority(rs.getString("id"), rs.getString("codename")))
                .single();
    }
}
