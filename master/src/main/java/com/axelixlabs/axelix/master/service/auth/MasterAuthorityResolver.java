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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfiguration;

/**
 * Implementation of {@link AuthorityResolver} that is supposed to handle master endpoints.
 *
 * @author Mikhail Polivakha
 */
public class MasterAuthorityResolver implements AuthorityResolver {

    private static final PathPatternParser PATH_PATTERN_PARSER;
    private static final List<RegisteredPattern> REGISTERED_PATTERNS;

    static {
        PATH_PATTERN_PARSER = new PathPatternParser();
        REGISTERED_PATTERNS = new ArrayList<>();

        // Users -> USERS_MANAGEMENT
        put(ApiPaths.UsersManagementApi.USERS_CREATE, HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT);
        put(ApiPaths.UsersManagementApi.USERS_DROP, HttpMethod.DELETE, DefaultAuthority.USERS_MANAGEMENT);
        put(ApiPaths.UsersManagementApi.USERS_UPDATE_USERNAME, HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT);
        put(ApiPaths.UsersManagementApi.USERS_UPDATE_EMAIL, HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT);
        put(ApiPaths.UsersManagementApi.USERS_UPDATE_PASSWORD, HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT);
        put(ApiPaths.UsersManagementApi.USERS_UPDATE_ROLE, HttpMethod.POST, DefaultAuthority.USERS_MANAGEMENT);

        // Users -> USERS_VIEW
        put(ApiPaths.UsersManagementApi.USERS_VIEW, HttpMethod.GET, DefaultAuthority.USERS_VIEW);

        // Caches
        put(ApiPaths.CachesApi.DISABLE_CACHE, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE);
        put(ApiPaths.CachesApi.ENABLE_CACHE, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE);
        put(ApiPaths.CachesApi.DISABLE_CACHE_MANAGER, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE);
        put(ApiPaths.CachesApi.ENABLE_CACHE_MANAGER, HttpMethod.POST, DefaultAuthority.CACHES_TOGGLE);
        put(ApiPaths.CachesApi.CACHE_NAME, HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR);
        put(ApiPaths.CachesApi.INSTANCE_ID, HttpMethod.DELETE, DefaultAuthority.CACHES_CLEAR);

        // Garbage Collector
        put(ApiPaths.GcLogFileApi.DISABLE_GC_LOGGING, HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR);
        put(ApiPaths.GcLogFileApi.ENABLE_GC_LOGGING, HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR);
        put(ApiPaths.GcLogFileApi.TRIGGER_GC, HttpMethod.POST, DefaultAuthority.GARBAGE_COLLECTOR);

        // ScheduledTasks
        put(ApiPaths.ScheduledTasksApi.DISABLE_TASK, HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        put(ApiPaths.ScheduledTasksApi.ENABLE_TASK, HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        put(ApiPaths.ScheduledTasksApi.EXECUTE, HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        put(
                ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION,
                HttpMethod.POST,
                DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        put(ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL, HttpMethod.POST, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    }

    @Override
    public Optional<Authority> resolve(String path, HttpMethod httpMethod) {

        if (path.startsWith(WebAutoConfiguration.EXTERNAL_API_PATH)) {
            path = path.substring(WebAutoConfiguration.EXTERNAL_API_PATH.length());
        }

        PathContainer pathContainer = PathContainer.parsePath(path);

        for (RegisteredPattern registered : REGISTERED_PATTERNS) {
            if (registered.method == httpMethod && registered.pathPattern.matches(pathContainer)) {
                return Optional.of(registered.authority);
            }
        }

        return Optional.empty();
    }

    private static void put(String apiPathPattern, HttpMethod method, Authority authority) {
        PathPattern pathPattern = PATH_PATTERN_PARSER.parse(apiPathPattern);
        REGISTERED_PATTERNS.add(new RegisteredPattern(method, pathPattern, authority));
    }

    private record RegisteredPattern(HttpMethod method, PathPattern pathPattern, Authority authority) {}
}
