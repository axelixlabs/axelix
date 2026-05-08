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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MasterAuthorityResolver}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class MasterAuthorityResolverTest {

    private AuthorityResolver subject;

    @BeforeEach
    void setUp() {
        subject = new MasterAuthorityResolver();
    }

    @ParameterizedTest
    @MethodSource("mappedEndpoints")
    void shouldResolveMappedAuthority(String path, HttpMethod httpMethod, DefaultAuthority authority) {
        // when/then.
        assertThat(subject.resolve(path, httpMethod)).contains(authority);
    }

    @ParameterizedTest
    @MethodSource("unmappedEndpoints")
    void shouldReturnEmptyWhenEndpointIsNotMapped(String path, HttpMethod httpMethod) {
        // when/then.
        assertThat(subject.resolve(path, httpMethod)).isEmpty();
    }

    private static Stream<Arguments> mappedEndpoints() {
        return Stream.of(
                // USERS_MANAGEMENT
                Arguments.of("/users-management/create", HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT),
                Arguments.of("/users-management/delete", HttpMethod.DELETE, DefaultAuthority.USERS_MANAGEMENT),
                Arguments.of("/users-management/update", HttpMethod.PUT, DefaultAuthority.USERS_MANAGEMENT),

                // USERS_VIEW
                Arguments.of("/users-management/feed", HttpMethod.GET, DefaultAuthority.USERS_VIEW),

                // CACHES_TOGGLE (servlet paths include /api/external; patterns are registered without it)
                Arguments.of("/api/external/caches/i/cm/cn/disable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/api/external/caches/i/cm/cn/enable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/api/external/caches/i/cm/disable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/api/external/caches/i/cm/enable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of("/caches/i/cm/cn/disable", HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),

                // CACHES_CLEAR
                Arguments.of("/api/external/caches/i/cache/cn", HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),
                Arguments.of("/api/external/caches/i", HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),

                // GARBAGE_COLLECTOR
                Arguments.of(
                        "/api/external/garbage-collector/logs/i/disable",
                        HttpMethod.POST,
                        DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of(
                        "/api/external/garbage-collector/logs/i/enable",
                        HttpMethod.POST,
                        DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of(
                        "/api/external/garbage-collector/i/trigger",
                        HttpMethod.POST,
                        DefaultAuthority.GARBAGE_COLLECTOR),

                // SCHEDULED_TASKS_MODIFY
                Arguments.of(
                        "/api/external/scheduled-tasks/i/disable",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/api/external/scheduled-tasks/i/enable",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/api/external/scheduled-tasks/i/execute",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/api/external/scheduled-tasks/i/modify/cron-expression",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        "/api/external/scheduled-tasks/i/modify/interval",
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),

                // THREAD_DUMP_TOGGLE
                Arguments.of(
                        "/api/external/thread-dump/i/thread-contention-monitoring/enable",
                        HttpMethod.POST,
                        DefaultAuthority.THREAD_DUMP_TOGGLE),
                Arguments.of(
                        "/api/external/thread-dump/i/thread-contention-monitoring/disable",
                        HttpMethod.POST,
                        DefaultAuthority.THREAD_DUMP_TOGGLE));
    }

    private static Stream<Arguments> unmappedEndpoints() {
        return Stream.of(
                // wrong method for known path
                Arguments.of("/api/external/caches/i/cm/cn/disable", HttpMethod.GET),
                Arguments.of("/api/external/scheduled-tasks/i/modify/cron-expression", HttpMethod.GET),

                // known but public endpoints
                Arguments.of("/api/external/caches/i", HttpMethod.GET),
                Arguments.of("/api/external/garbage-collector/logs/i/status", HttpMethod.GET),

                // unknown endpoint
                Arguments.of("/unknown/path", HttpMethod.POST));
    }
}
