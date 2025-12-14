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
package com.nucleonforge.axile.master.service.auth;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.master.autoconfiguration.auth.CookieProperties;
import com.nucleonforge.axile.master.autoconfiguration.auth.JwtProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultCookieService}.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@TestPropertySource(properties = {"axile.master.auth.cookie.domain=example.com", "axile.master.auth.jwt.lifespan=12h"})
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
