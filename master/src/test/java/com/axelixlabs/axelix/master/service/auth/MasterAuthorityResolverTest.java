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
import com.axelixlabs.axelix.master.api.external.ApiPaths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MasterAuthorityResolver}.
 *
 * @author Mikhail Polivakha
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
                // CACHES_TOGGLE
                Arguments.of(ApiPaths.CachesApi.DISABLE_CACHE, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of(ApiPaths.CachesApi.ENABLE_CACHE, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of(ApiPaths.CachesApi.DISABLE_CACHE_MANAGER, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),
                Arguments.of(ApiPaths.CachesApi.ENABLE_CACHE_MANAGER, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE),

                // CACHES_CLEAR
                Arguments.of(ApiPaths.CachesApi.CACHE_NAME, HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),
                Arguments.of(ApiPaths.CachesApi.INSTANCE_ID, HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR),

                // GARBAGE_COLLECTOR
                Arguments.of(
                        ApiPaths.GcLogFileApi.DISABLE_GC_LOGGING, HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of(
                        ApiPaths.GcLogFileApi.ENABLE_GC_LOGGING, HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),
                Arguments.of(ApiPaths.GcLogFileApi.TRIGGER_GC, HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR),

                // PROPERTY_VALUE_MUTATE
                Arguments.of(
                        ApiPaths.PropertyManagementApi.INSTANCE_ID,
                        HttpMethod.POST,
                        DefaultAuthority.PROPERTY_VALUE_MUTATE),

                // SCHEDULED_TASKS_MODIFY
                Arguments.of(
                        ApiPaths.ScheduledTasksApi.DISABLE_TASK,
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        ApiPaths.ScheduledTasksApi.ENABLE_TASK,
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        ApiPaths.ScheduledTasksApi.EXECUTE, HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION,
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY),
                Arguments.of(
                        ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL,
                        HttpMethod.POST,
                        DefaultAuthority.SCHEDULED_TASKS_MODIFY));
    }

    private static Stream<Arguments> unmappedEndpoints() {
        return Stream.of(
                // wrong method for known path
                Arguments.of(ApiPaths.CachesApi.DISABLE_CACHE, HttpMethod.GET),
                Arguments.of(ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION, HttpMethod.GET),

                // known but public endpoints
                Arguments.of(ApiPaths.CachesApi.INSTANCE_ID, HttpMethod.GET),
                Arguments.of(ApiPaths.GcLogFileApi.STATUS_GC_LOGGING, HttpMethod.GET),

                // unknown endpoint
                Arguments.of("/unknown/path", HttpMethod.POST));
    }
}
