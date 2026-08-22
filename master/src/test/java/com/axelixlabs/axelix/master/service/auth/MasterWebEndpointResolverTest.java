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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MasterWebEndpointResolver}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class MasterWebEndpointResolverTest {

    private final MasterWebEndpointResolver resolver = new MasterWebEndpointResolver(MasterWebEndpoints.oss());

    @ParameterizedTest
    @MethodSource("authorityProtectedEndpoints")
    void shouldResolveEndpointCarryingItsRequiredAuthority(
            String path, HttpMethod httpMethod, DefaultAuthority authority) {
        // when/then.
        assertThat(resolver.resolveEndpoint(path, httpMethod))
                .hasValueSatisfying(endpoint -> assertThat(endpoint.authority()).isEqualTo(authority));
    }

    @ParameterizedTest
    @MethodSource("unprotectedEndpoints")
    void shouldResolveUnprotectedEndpointWithoutAuthority(String path, HttpMethod httpMethod) {
        // when/then.
        assertThat(resolver.resolveEndpoint(path, httpMethod))
                .hasValueSatisfying(endpoint -> assertThat(endpoint.authority()).isNull());
    }

    @ParameterizedTest
    @MethodSource("unresolvableEndpoints")
    void shouldNotResolveUnknownOrWrongMethodEndpoints(String path, HttpMethod httpMethod) {
        // when/then.
        assertThat(resolver.resolveEndpoint(path, httpMethod)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("operationCodes")
    void shouldResolveEndpointWithOperationCode(String path, HttpMethod httpMethod, String operationCode) {
        // when/then.
        assertThat(resolver.resolveEndpoint(path, httpMethod))
                .hasValueSatisfying(
                        endpoint -> assertThat(endpoint.operationCode()).isEqualTo(operationCode));
    }

    private static Stream<Arguments> authorityProtectedEndpoints() {
        return Stream.of(
                // USERS_MANAGEMENT
                Arguments.of("/users-management/create", HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT),
                Arguments.of("/users-management/delete", HttpMethod.DELETE, DefaultAuthority.USERS_MANAGEMENT),
                Arguments.of("/users-management/status", HttpMethod.PUT, DefaultAuthority.USERS_MANAGEMENT),
                Arguments.of("/users-management/update", HttpMethod.PUT, DefaultAuthority.USERS_MANAGEMENT),

                // USERS_VIEW
                Arguments.of("/users/feed", HttpMethod.GET, DefaultAuthority.USERS_VIEW),

                // CACHES_TOGGLE
                Arguments.of("/caches/i/cm/cn/disable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/caches/i/cm/cn/enable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/caches/i/cm/disable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/caches/i/cm/enable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),

                // CACHES_CLEAR
                Arguments.of("/caches/i/cache/cn", HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),
                Arguments.of("/caches/i", HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),

                // GARBAGE_COLLECTOR
                Arguments.of("/garbage-collector/logs/i/disable", HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of("/garbage-collector/logs/i/enable", HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of("/garbage-collector/i/trigger", HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),

                // SCHEDULED_TASKS_MODIFY
                Arguments.of("/scheduled-tasks/i/disable", HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of("/scheduled-tasks/i/enable", HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of("/scheduled-tasks/i/execute", HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/scheduled-tasks/i/modify/cron-expression",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/scheduled-tasks/i/modify/interval",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY));
    }

    private static Stream<Arguments> unprotectedEndpoints() {
        return Stream.of(
                Arguments.of("/env/feed/i", HttpMethod.GET),
                Arguments.of("/caches/i", HttpMethod.GET),
                Arguments.of("/garbage-collector/logs/i/status", HttpMethod.GET),
                Arguments.of("/users/login", HttpMethod.POST));
    }

    private static Stream<Arguments> unresolvableEndpoints() {
        return Stream.of(
                // wrong method for known path
                Arguments.of("/caches/i/cm/cn/disable", HttpMethod.GET),
                Arguments.of("/scheduled-tasks/i/modify/cron-expression", HttpMethod.GET),

                // unknown endpoint
                Arguments.of("/unknown/path", HttpMethod.POST));
    }

    private static Stream<Arguments> operationCodes() {
        return Stream.of(
                Arguments.of("/env/feed/i", HttpMethod.GET, "environment:read"),
                Arguments.of("/beans/feed/i", HttpMethod.GET, "beans:read"),
                Arguments.of("/caches/i", HttpMethod.DELETE, "caches:clear-all"),
                Arguments.of("/caches/i", HttpMethod.GET, "caches:read"),
                Arguments.of("/users-management/create", HttpMethod.POST, "users:create"),
                Arguments.of("/users/login", HttpMethod.POST, "auth:login"));
    }
}
