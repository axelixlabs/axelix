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

import org.jspecify.annotations.NullMarked;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.OssAuthority;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * Default {@link AuthorityResolver}. Determines the required {@link Authority} based on the request path.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@NullMarked
public class DefaultAuthorityResolver implements AuthorityResolver {

    private static final Map<ActuatorEndpoint, Authority> PATH_MAPPINGS;

    static {
        Map<ActuatorEndpoint, Authority> map = new LinkedHashMap<>();

        // TODO:
        //  Static configuration like here is not extensible at all. It is going to become a problem
        //  while building the unified distro

        // SCHEDULED_TASKS_MODIFY
        map.put(ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK, OssAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK, OssAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.EXECUTE_SCHEDULED_TASK, OssAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.ENABLE_SCHEDULED_TASK, OssAuthority.SCHEDULED_TASKS_MODIFY);
        map.put(ActuatorEndpoints.DISABLE_SCHEDULED_TASK, OssAuthority.SCHEDULED_TASKS_MODIFY);

        // CACHES_CLEAR
        map.put(ActuatorEndpoints.CLEAR_ALL_CACHES, OssAuthority.CACHES_CLEAR);
        map.put(ActuatorEndpoints.CLEAR_SINGLE_CACHE, OssAuthority.CACHES_CLEAR);
        map.put(ActuatorEndpoints.CLEAR_SINGLE_CACHE_MANAGER, OssAuthority.CACHES_CLEAR);

        // CACHES_TOGGLE
        map.put(ActuatorEndpoints.ENABLE_CACHE, OssAuthority.CACHES_TOGGLE);
        map.put(ActuatorEndpoints.DISABLE_CACHE, OssAuthority.CACHES_TOGGLE);
        map.put(ActuatorEndpoints.ENABLE_CACHE_MANAGER, OssAuthority.CACHES_TOGGLE);
        map.put(ActuatorEndpoints.DISABLE_CACHES_MANAGER, OssAuthority.CACHES_TOGGLE);

        // GARBAGE_COLLECTOR
        map.put(ActuatorEndpoints.GC_TRIGGER, OssAuthority.GARBAGE_COLLECTOR);
        map.put(ActuatorEndpoints.DISABLE_GC_LOGGING, OssAuthority.GARBAGE_COLLECTOR);
        map.put(ActuatorEndpoints.ENABLE_GC_LOGGING, OssAuthority.GARBAGE_COLLECTOR);

        PATH_MAPPINGS = Collections.unmodifiableMap(map);
    }

    private final EndpointPathMatcher pathMatcher;

    public DefaultAuthorityResolver(EndpointPathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    /**
     * Important: The requestPath parameter is expected to be relative to the actuator base path
     * WebEndpointProperties#getBasePath().
     * <p>
     * For example, if the full request is "/actuator/axelix-beans", the path passed here should be "/axelix-beans".
     */
    @Override
    public Optional<Authority> resolve(String relativeRequestPath, HttpMethod httpMethod) {

        // TODO: well, technically we probably can resolve via simple map lookup, I guess...
        return PATH_MAPPINGS.entrySet().stream()
                .filter(entry -> entry.getKey().httpMethod().equals(httpMethod))
                .filter(entry -> pathMatcher.matches(entry.getKey().path().originalUrl(), relativeRequestPath))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
