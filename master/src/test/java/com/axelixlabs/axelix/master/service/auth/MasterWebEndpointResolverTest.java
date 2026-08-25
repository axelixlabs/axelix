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
    @MethodSource("resolvableRequests")
    void shouldResolveEndpointForMatchingPathAndMethod(String path, HttpMethod httpMethod, MasterWebEndpoint expected) {
        // when/then.
        assertThat(resolver.resolveEndpoint(path, httpMethod)).contains(expected);
    }

    @ParameterizedTest
    @MethodSource("unresolvableRequests")
    void shouldNotResolveEndpointForUnknownPathOrMethod(String path, HttpMethod httpMethod) {
        // when/then.
        assertThat(resolver.resolveEndpoint(path, httpMethod)).isEmpty();
    }

    private static Stream<Arguments> resolvableRequests() {
        return Stream.of(
                // Non-templated paths.
                Arguments.of("/applications/grid", HttpMethod.GET, MasterWebEndpoints.INSTANCES_READ),
                Arguments.of("/users/login", HttpMethod.POST, MasterWebEndpoints.LOCAL_LOGIN),
                Arguments.of("/dashboard", HttpMethod.GET, MasterWebEndpoints.DASHBOARD_READ),

                // Single trailing template variable.
                Arguments.of("/env/feed/42", HttpMethod.GET, MasterWebEndpoints.ENVIRONMENT_READ),
                Arguments.of("/caches/42", HttpMethod.DELETE, MasterWebEndpoints.CACHES_CLEAR_ALL),

                // Template variable followed by a static segment.
                Arguments.of("/garbage-collector/logs/42/enable", HttpMethod.POST, MasterWebEndpoints.GC_LOG_ENABLE),
                Arguments.of(
                        "/scheduled-tasks/42/modify/cron-expression",
                        HttpMethod.POST,
                        MasterWebEndpoints.SCHEDULED_TASK_MODIFY_CRON),

                // Multiple template variables.
                Arguments.of("/metrics/42/jvm.memory.used", HttpMethod.GET, MasterWebEndpoints.METRIC_READ_ONE),
                Arguments.of("/loggers/42/logger/com.foo.Bar", HttpMethod.GET, MasterWebEndpoints.LOGGER_READ_ONE),
                Arguments.of("/caches/42/cache/orders", HttpMethod.DELETE, MasterWebEndpoints.CACHE_CLEAR_ONE),
                Arguments.of(
                        "/caches/42/cacheManager/orders/disable", HttpMethod.POST, MasterWebEndpoints.CACHE_DISABLE),

                // Multiple template variables followed by a static segment.
                Arguments.of("/loggers/42/logger/com.foo.Bar/reset", HttpMethod.POST, MasterWebEndpoints.LOGGER_RESET));
    }

    private static Stream<Arguments> unresolvableRequests() {
        return Stream.of(
                // Completely unknown path.
                Arguments.of("/unknown/path", HttpMethod.POST),

                // Known path, but wrong HTTP method.
                Arguments.of("/users/login", HttpMethod.GET),
                Arguments.of("/caches/42/cacheManager/orders/disable", HttpMethod.GET),

                // Known prefix, but the path does not match any pattern in full.
                Arguments.of("/caches", HttpMethod.GET),
                Arguments.of("/env/feed", HttpMethod.GET),
                Arguments.of("/dashboard/unknown", HttpMethod.GET),

                // A template variable does not match extra trailing segments.
                Arguments.of("/env/feed/42/extra", HttpMethod.GET));
    }
}
