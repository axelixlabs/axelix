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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.net.URI;
import java.security.Principal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.common.testfixtures.UserUtils;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.JwtAuthAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.SecurityContextExecutorAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReactiveJwtAuthorizationFilter}.
 *
 * @author Nikita Kirillov
 * @since 20.10.2026
 */
class ReactiveJwtAuthorizationFilterTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withPropertyValues(
                    "axelix.sbs.auth.jwt.algorithm=HMAC512",
                    "axelix.sbs.auth.jwt.signing-key="
                            + "22573444698685aa77750b88ad6e99ef1f94a7a909bc7df63f7a80666208c201ab7a584e6e05e6c9d0aa94b723f843ff")
            .withConfiguration(AutoConfigurations.of(
                    JwtAuthAutoConfiguration.class,
                    SecurityContextExecutorAutoConfiguration.class,
                    EndpointAutoConfiguration.class,
                    WebEndpointAutoConfiguration.class,
                    ManagementContextAutoConfiguration.class));

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/actuator/axelix-env",
                "/actuator/%61xelix-env",
                "/actuator;x=1/axelix-env",
                "/actuator/axelix-env;x=1",
                "/actuator/%61xelix-heap-dump"
            })
    void shouldRejectAxelixRequestWithoutToken(String path) {
        contextRunner.run(context -> {
            ReactiveJwtAuthorizationFilter filter = context.getBean(ReactiveJwtAuthorizationFilter.class);
            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.method(HttpMethod.GET, URI.create(path)));
            AtomicBoolean passed = new AtomicBoolean();

            filter.filter(exchange, e -> {
                        passed.set(true);
                        return e.getResponse().setComplete();
                    })
                    .block();

            assertThat(passed).isFalse();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"/greetings/world", "/actuator/health"})
    void shouldPassNonAxelixRequest(String path) {
        contextRunner.run(context -> {
            ReactiveJwtAuthorizationFilter filter = context.getBean(ReactiveJwtAuthorizationFilter.class);
            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.method(HttpMethod.GET, URI.create(path)));
            AtomicBoolean passed = new AtomicBoolean();

            filter.filter(exchange, e -> {
                        passed.set(true);
                        return e.getResponse().setComplete();
                    })
                    .block();

            assertThat(passed).isTrue();
        });
    }

    @Test
    void shouldPassAuthorizedRequestWithAxelixPrincipal() {
        contextRunner.run(context -> {
            ReactiveJwtAuthorizationFilter filter = context.getBean(ReactiveJwtAuthorizationFilter.class);
            User user = UserUtils.withPassword("testUser", "testPassword", Set.of(TestRoles.VIEWER));
            String token = context.getBean(JwtEncoderService.class).generateToken(user);
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/actuator/axelix-env").header(HttpHeaders.AUTHORIZATION, bearer(token)));
            AtomicReference<Principal> principal = new AtomicReference<>();

            filter.filter(
                            exchange,
                            e -> e.getPrincipal()
                                    .doOnNext(principal::set)
                                    .then(e.getResponse().setComplete()))
                    .block();

            assertThat(principal.get()).isInstanceOfSatisfying(AxelixPrincipal.class, axelixPrincipal -> {
                assertThat(axelixPrincipal.getName()).isEqualTo("testUser");
                assertThat(axelixPrincipal.securityContext().token()).isEqualTo(token);
            });
        });
    }

    @Test
    void shouldRejectRequestWithoutRequiredAuthority() {
        contextRunner.run(context -> {
            ReactiveJwtAuthorizationFilter filter = context.getBean(ReactiveJwtAuthorizationFilter.class);
            User user = UserUtils.withPassword("testUser", "testPassword", Set.of(TestRoles.VIEWER));
            String token = context.getBean(JwtEncoderService.class).generateToken(user);
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.delete("/actuator/axelix-caches/mainCacheManager/cache/clear")
                            .header(HttpHeaders.AUTHORIZATION, bearer(token)));
            AtomicBoolean passed = new AtomicBoolean();

            filter.filter(exchange, e -> {
                        passed.set(true);
                        return e.getResponse().setComplete();
                    })
                    .block();

            assertThat(passed).isFalse();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        });
    }

    @Test
    void shouldRejectRequestWithTamperedToken() {
        contextRunner.run(context -> {
            ReactiveJwtAuthorizationFilter filter = context.getBean(ReactiveJwtAuthorizationFilter.class);
            User user = UserUtils.withPassword("testUser", "testPassword", Set.of(TestRoles.ADMIN));
            String token = context.getBean(JwtEncoderService.class).generateToken(user);
            MockServerWebExchange exchange =
                    MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/axelix-env")
                            .header(HttpHeaders.AUTHORIZATION, bearer(token + "x")));
            AtomicBoolean passed = new AtomicBoolean();

            filter.filter(exchange, e -> {
                        passed.set(true);
                        return e.getResponse().setComplete();
                    })
                    .block();

            assertThat(passed).isFalse();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        });
    }

    private static String bearer(String token) {
        return AuthenticationSchemes.BEARER.prefix() + token;
    }
}
