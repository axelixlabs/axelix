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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;

/**
 * Default {@link AuthorityResolver}. Determines the required {@link Authority} based on the request path.
 *
 * @author Sergey Cherkasov
 */
public class DefaultAuthorityResolver implements AuthorityResolver {

    private final Map<ActuatorEndpoint, Authority> pathMappings;
    private final BiFunction<String, String, Boolean> pathMatcher;

    public DefaultAuthorityResolver(BiFunction<String, String, Boolean> pathMatcher) {
        this.pathMatcher = pathMatcher;
        this.pathMappings = buildPathMappings();
    }

    @Override
    public Optional<Authority> resolve(String path) {
        return pathMappings.entrySet().stream()
                .filter(entry -> pathMatcher.apply(entry.getKey().path().originalUrl(), path))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private Map<ActuatorEndpoint, Authority> buildPathMappings() {
        Map<ActuatorEndpoint, Authority> map = new LinkedHashMap<>();

        // ENV_VALUES_READ
        map.put(ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, DefaultAuthority.ENV_VALUES_READ);

        // CONFIG_PROPS_VALUES_READ
        map.put(ActuatorEndpoints.GET_CONFIG_PROPS, DefaultAuthority.CONFIG_PROPS_VALUES_READ);

        // PROPERTY_VALUE_MUTATE
        map.put(ActuatorEndpoints.PROPERTY_MANAGEMENT, DefaultAuthority.PROPERTY_VALUE_MUTATE);

        // SCHEDULED_TASKS_MODIFY
        map.put(ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.EXECUTE_SCHEDULED_TASK, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.ENABLE_SCHEDULED_TASK, DefaultAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.DISABLE_SCHEDULED_TASK, DefaultAuthority.SCHEDULED_TASKS_MODIFY);

        // CONDITIONS_READ
        map.put(ActuatorEndpoints.GET_CONDITIONS, DefaultAuthority.CONDITIONS_READ);

        // CACHES_CLEAR
        map.put(ActuatorEndpoints.CLEAR_ALL_CACHES, DefaultAuthority.CACHES_CLEAR);
        map.put(ActuatorEndpoints.CLEAR_SINGLE_CACHE, DefaultAuthority.CACHES_CLEAR);
        map.put(ActuatorEndpoints.CLEAR_SINGLE_CACHE_MANAGER, DefaultAuthority.CACHES_CLEAR);

        // CACHES_TOGGLE
        map.put(ActuatorEndpoints.ENABLE_CACHE, DefaultAuthority.CACHES_TOGGLE);
        map.put(ActuatorEndpoints.DISABLE_CACHE, DefaultAuthority.CACHES_TOGGLE);
        map.put(ActuatorEndpoints.ENABLE_CACHE_MANAGER, DefaultAuthority.CACHES_TOGGLE);
        map.put(ActuatorEndpoints.DISABLE_CACHES_MANAGER, DefaultAuthority.CACHES_TOGGLE);

        // GARBAGE_COLLECTOR
        map.put(ActuatorEndpoints.GC_TRIGGER, DefaultAuthority.GARBAGE_COLLECTOR);
        map.put(ActuatorEndpoints.DISABLE_GC_LOGGING, DefaultAuthority.GARBAGE_COLLECTOR);
        map.put(ActuatorEndpoints.ENABLE_GC_LOGGING, DefaultAuthority.GARBAGE_COLLECTOR);

        return Collections.unmodifiableMap(map);
    }
}
