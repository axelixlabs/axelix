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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.api.external.ApiPaths;

/**
 * Implementation of {@link AuthorityResolver} that is supposed to handle master endpoints.
 *
 * @author Mikhail Polivakha
 */
public class MasterAuthorityResolver implements AuthorityResolver {

    private static final Map<ApiEndpointKey, Authority> AUTHORITIES_MAPPINGS;

    static {
        // It is okay to use HashMap, since map is not modified under contention
        AUTHORITIES_MAPPINGS = new HashMap<>();

        // Caches
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.CachesApi.DISABLE_CACHE, HttpMethod.POST), DefaultAuthority.CACHES_TOGGLE);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.CachesApi.ENABLE_CACHE, HttpMethod.POST), DefaultAuthority.CACHES_TOGGLE);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.CachesApi.DISABLE_CACHE_MANAGER, HttpMethod.POST),
                DefaultAuthority.CACHES_TOGGLE);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.CachesApi.ENABLE_CACHE_MANAGER, HttpMethod.POST),
                DefaultAuthority.CACHES_TOGGLE);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.CachesApi.CACHE_NAME, HttpMethod.DELETE), DefaultAuthority.CACHES_CLEAR);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.CachesApi.INSTANCE_ID, HttpMethod.DELETE), DefaultAuthority.CACHES_CLEAR);

        // Garbage Collector
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.GcLogFileApi.DISABLE_GC_LOGGING, HttpMethod.POST),
                DefaultAuthority.GARBAGE_COLLECTOR);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.GcLogFileApi.ENABLE_GC_LOGGING, HttpMethod.POST),
                DefaultAuthority.GARBAGE_COLLECTOR);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.GcLogFileApi.TRIGGER_GC, HttpMethod.POST),
                DefaultAuthority.GARBAGE_COLLECTOR);

        // @ConfigurationProperties value mutation
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.PropertyManagementApi.INSTANCE_ID, HttpMethod.POST),
                DefaultAuthority.PROPERTY_VALUE_MUTATE);

        // ScheduledTasks
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.ScheduledTasksApi.DISABLE_TASK, HttpMethod.POST),
                DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.ScheduledTasksApi.ENABLE_TASK, HttpMethod.POST),
                DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.ScheduledTasksApi.EXECUTE, HttpMethod.POST),
                DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION, HttpMethod.POST),
                DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        AUTHORITIES_MAPPINGS.put(
                ApiEndpointKey.of(ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL, HttpMethod.POST),
                DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    }

    @Override
    public Optional<Authority> resolve(String path, HttpMethod httpMethod) {
        return Optional.ofNullable(AUTHORITIES_MAPPINGS.get(ApiEndpointKey.of(path, httpMethod)));
    }

    /**
     * The unique key by which, we assume, each external endpoint on the master side can be uniquely identified.
     *
     * @param path the servlet path to the endpoint (e.g. /beans/feed/{instanceId})
     * @param method the HTTP method that endpoint is supposed to process (POST, GET etc.)
     */
    record ApiEndpointKey(String path, HttpMethod method) {

        public static ApiEndpointKey of(String path, HttpMethod method) {
            return new ApiEndpointKey(path, method);
        }
    }
}
