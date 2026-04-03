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

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultAuthorityResolver}.
 *
 * @author Cherkasov Sergey
 */
public class DefaultAuthorityResolverTest {

    private AuthorityResolver authorityResolver;

    @BeforeEach
    void setUp() {
        authorityResolver = new DefaultAuthorityResolver((pathTemplate, actualPath) -> {
            PathPattern pattern = new PathPatternParser().parse(pathTemplate);
            return pattern.matchAndExtract(PathContainer.parsePath(actualPath)) != null;
        });
    }

    @ParameterizedTest
    @MethodSource("defaultAuthority")
    void shouldResolveAuthority(String path, HttpMethod httpMethod, DefaultAuthority authority) {
        assertThat(authorityResolver.resolve(path, httpMethod)).contains(authority);
    }

    private static Stream<Arguments> defaultAuthority() {
        return Stream.of(
                // ENV_VALUES_READ
                Arguments.of("/axelix-env", HttpMethod.GET, DefaultAuthority.ENV_VALUES_READ),

                // CONFIG_PROPS_VALUES_READ
                Arguments.of("/axelix-configprops", HttpMethod.GET, DefaultAuthority.CONFIG_PROPS_VALUES_READ),

                // PROPERTY_VALUE_MUTATE
                Arguments.of("/axelix-property-management", HttpMethod.POST, DefaultAuthority.PROPERTY_VALUE_MUTATE),

                // SCHEDULED_TASKS_MODIFY
                Arguments.of(
                        "/axelix-scheduled-tasks/modify/cron-expression",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/axelix-scheduled-tasks/modify/interval",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/axelix-scheduled-tasks/enable", HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/axelix-scheduled-tasks/disable", HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/axelix-scheduled-tasks/execute", HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),

                // CACHES_CLEAR
                Arguments.of("/axelix-caches/clear", HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),
                Arguments.of(
                        "/axelix-caches/{cacheManagerName}/{cacheName}/clear",
                        HttpMethod.DELETE,
                        DefaultAuthority.CACHES_CLEAR),
                Arguments.of(
                        "/axelix-caches/cacheManager/cacheName/clear",
                        HttpMethod.DELETE,
                        DefaultAuthority.CACHES_CLEAR),
                Arguments.of("/axelix-caches/cacheManager/clear-all", HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),

                // CACHES_TOGGLE
                Arguments.of(
                        "/axelix-caches/{cacheManagerName}/{cacheName}/enable",
                        HttpMethod.POST,
                        DefaultAuthority.CACHES_TOGGLE),
                Arguments.of(
                        "/axelix-caches/cacheManager/cacheName/enable",
                        HttpMethod.POST,
                        DefaultAuthority.CACHES_TOGGLE),
                Arguments.of(
                        "/axelix-caches/{cacheManagerName}/{cacheName}/disable",
                        HttpMethod.POST,
                        DefaultAuthority.CACHES_TOGGLE),
                Arguments.of(
                        "/axelix-caches/cacheManager/cacheName/disable",
                        HttpMethod.POST,
                        DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/cacheManager/enable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/cacheManager/disable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),

                // GC
                Arguments.of("/axelix-gc/trigger", HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of("/axelix-gc/log/enable", HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of("/axelix-gc/log/disable", HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR));
    }

    @ParameterizedTest
    @MethodSource("pathsAvailableToEveryone")
    void shouldResolveToEmpty(String path, HttpMethod httpMethod) {
        assertThat(authorityResolver.resolve(path, httpMethod)).isEmpty();
    }

    private static Stream<Arguments> pathsAvailableToEveryone() {
        return Stream.of(
                Arguments.of("/axelix-scheduled-tasks", HttpMethod.GET),
                Arguments.of("/axelix-caches", HttpMethod.GET),
                Arguments.of("/axelix-gc", HttpMethod.GET),
                Arguments.of("/axelix-gc/log/status", HttpMethod.GET),
                Arguments.of("/axelix-gc/log/file", HttpMethod.GET),
                Arguments.of("/axelix-beans", HttpMethod.GET),
                Arguments.of("/axelix-heap-dump", HttpMethod.GET),
                Arguments.of("/axelix-details", HttpMethod.GET),
                Arguments.of("/axelix-metadata", HttpMethod.GET),
                Arguments.of("/axelix-loggers", HttpMethod.GET),
                Arguments.of("/axelix-loggers/{logger.name}", HttpMethod.GET),
                Arguments.of("/axelix-loggers/logger.name", HttpMethod.GET),
                Arguments.of("/axelix-loggers/reset/{logger.name}", HttpMethod.GET),
                Arguments.of("/axelix-loggers/reset/logger.name", HttpMethod.GET),
                Arguments.of("/axelix-metrics", HttpMethod.GET),
                Arguments.of("/axelix-metrics/{metric.name}", HttpMethod.GET),
                Arguments.of("/axelix-metrics/jvm.buffer.count", HttpMethod.GET),
                Arguments.of("/axelix-thread-dump", HttpMethod.GET),
                Arguments.of("/axelix-thread-dump/enable", HttpMethod.GET),
                Arguments.of("/axelix-thread-dump/disable", HttpMethod.GET),
                Arguments.of("/axelix-transactions-monitoring", HttpMethod.GET),
                Arguments.of("/axelix-feign", HttpMethod.GET));
    }
}
