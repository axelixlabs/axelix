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
    void shouldResolveAuthority(String path, DefaultAuthority authority) {
        assertThat(authorityResolver.resolve(path)).contains(authority);
    }

    private static Stream<Arguments> defaultAuthority() {
        return Stream.of(
                // ENV_VALUES_READ
                Arguments.of("/axelix-env", DefaultAuthority.ENV_VALUES_READ),

                // CONFIG_PROPS_VALUES_READ
                Arguments.of("/axelix-configprops", DefaultAuthority.CONFIG_PROPS_VALUES_READ),

                // CONDITIONS_READ
                Arguments.of("/axelix-conditions", DefaultAuthority.CONDITIONS_READ),

                // PROPERTY_VALUE_MUTATE
                Arguments.of("/axelix-property-management", DefaultAuthority.PROPERTY_VALUE_MUTATE),

                // SCHEDULED_TASKS_MODIFY
                Arguments.of("/axelix-scheduled-tasks/modify/cron-expression", DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of("/axelix-scheduled-tasks/modify/interval", DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of("/axelix-scheduled-tasks/enable", DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of("/axelix-scheduled-tasks/disable", DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of("/axelix-scheduled-tasks/execute", DefaultAuthority.SCHEDULED_TASKS_MODIFY),

                // CACHES_CLEAR
                Arguments.of("/axelix-caches/clear", DefaultAuthority.CACHES_CLEAR),
                Arguments.of("/axelix-caches/{cacheManagerName}/{cacheName}/clear", DefaultAuthority.CACHES_CLEAR),
                Arguments.of("/axelix-caches/cacheManager/cacheName/clear", DefaultAuthority.CACHES_CLEAR),
                Arguments.of("/axelix-caches/cacheManager/clear-all", DefaultAuthority.CACHES_CLEAR),

                // CACHES_TOGGLE
                Arguments.of("/axelix-caches/{cacheManagerName}/{cacheName}/enable", DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/cacheManager/cacheName/enable", DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/{cacheManagerName}/{cacheName}/disable", DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/cacheManager/cacheName/disable", DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/cacheManager/enable", DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/axelix-caches/cacheManager/disable", DefaultAuthority.CACHES_TOGGLE),

                // GC
                Arguments.of("/axelix-gc/trigger", DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of("/axelix-gc/log/enable", DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of("/axelix-gc/log/disable", DefaultAuthority.GARBAGE_COLLECTOR));
    }

    @ParameterizedTest
    @MethodSource("pathsAvailableToEveryone")
    void shouldResolveToEmpty(String path) {
        assertThat(authorityResolver.resolve(path)).isEmpty();
    }

    private static Stream<Arguments> pathsAvailableToEveryone() {
        return Stream.of(
                Arguments.of("/axelix-scheduled-tasks"),
                Arguments.of("/axelix-caches"),
                Arguments.of("/axelix-gc"),
                Arguments.of("/axelix-gc/log/status"),
                Arguments.of("/axelix-gc/log/file"),
                Arguments.of("/axelix-beans"),
                Arguments.of("/axelix-heap-dump"),
                Arguments.of("/axelix-details"),
                Arguments.of("/axelix-metadata"),
                Arguments.of("/axelix-loggers"),
                Arguments.of("/axelix-loggers/{logger.name}"),
                Arguments.of("/axelix-loggers/logger.name"),
                Arguments.of("/axelix-loggers/reset/{logger.name}"),
                Arguments.of("/axelix-loggers/reset/logger.name"),
                Arguments.of("/axelix-metrics"),
                Arguments.of("/axelix-metrics/{metric.name}"),
                Arguments.of("/axelix-metrics/jvm.buffer.count"),
                Arguments.of("/axelix-thread-dump"),
                Arguments.of("/axelix-thread-dump/enable"),
                Arguments.of("/axelix-thread-dump/disable"),
                Arguments.of("/axelix-transactions-monitoring"),
                Arguments.of("/axelix-feign"));
    }
}
