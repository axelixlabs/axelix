/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.auth;

import java.util.Map;
import java.util.Optional;

import org.springframework.util.AntPathMatcher;

import com.nucleonforge.axelix.common.auth.core.Authority;
import com.nucleonforge.axelix.common.auth.core.DefaultAuthority;

/**
 * Default implementation of {@link AuthorityResolver}.
 *
 * @see DefaultAuthority
 * @see AntPathMatcher
 * @since 29.07.2025
 * @author Nikita Kirillov
 */
public class DefaultAuthorityResolver implements AuthorityResolver {

    private final Map<String, Authority> pathAuthoritiesMap = Map.ofEntries(
            Map.entry("/actuator/axelix-beans", DefaultAuthority.BEANS),
            Map.entry("/actuator/axelix-caches/**", DefaultAuthority.CACHES),
            Map.entry("/actuator/cache-dispatcher/**", DefaultAuthority.CACHE_DISPATCHER),
            Map.entry("/actuator/property-management/**", DefaultAuthority.PROPERTY_MANAGEMENT),
            Map.entry("/actuator/profile-management/**", DefaultAuthority.PROFILE_MANAGEMENT),
            Map.entry("/actuator/health/**", DefaultAuthority.HEALTH),
            Map.entry("/actuator/info", DefaultAuthority.INFO),
            Map.entry("/actuator/axelix-conditions", DefaultAuthority.CONDITIONS),
            Map.entry("/actuator/axelix-configprops", DefaultAuthority.CONFIGPROPS),
            Map.entry("/actuator/axelix-details", DefaultAuthority.DETAILS),
            Map.entry("/actuator/axelix-env/**", DefaultAuthority.ENV),
            Map.entry("/actuator/heapdump", DefaultAuthority.HEAP_DUMP),
            Map.entry("/actuator/threaddump", DefaultAuthority.THREAD_DUMP),
            Map.entry("/actuator/axelix-metrics/**", DefaultAuthority.METRICS),
            Map.entry("/actuator/loggers/**", DefaultAuthority.LOGGERS),
            Map.entry("/actuator/mappings", DefaultAuthority.MAPPINGS),
            Map.entry("/actuator/axelix-scheduledtasks/**", DefaultAuthority.SCHEDULED_TASKS),
            Map.entry("/actuator/scheduled-tasks-management/**", DefaultAuthority.SCHEDULED_TASKS_MANAGEMENT));

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Optional<Authority> resolve(String path) {
        return pathAuthoritiesMap.entrySet().stream()
                .filter(entry -> matcher.match(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
