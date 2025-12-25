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
package com.nucleonforge.axile.common.auth.spi.verification;

import java.util.Map;
import java.util.Optional;

import org.springframework.util.AntPathMatcher;

import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.DefaultAuthority;

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
            Map.entry("/actuator/beans", DefaultAuthority.BEANS),
            Map.entry("/actuator/axile-caches/**", DefaultAuthority.CACHES),
            Map.entry("/actuator/cache-dispatcher/**", DefaultAuthority.CACHE_DISPATCHER),
            Map.entry("/actuator/property-management/**", DefaultAuthority.PROPERTY_MANAGEMENT),
            Map.entry("/actuator/profile-management/**", DefaultAuthority.PROFILE_MANAGEMENT),
            Map.entry("/actuator/health/**", DefaultAuthority.HEALTH),
            Map.entry("/actuator/info", DefaultAuthority.INFO),
            Map.entry("/actuator/conditions", DefaultAuthority.CONDITIONS),
            Map.entry("/actuator/configprops", DefaultAuthority.CONFIGPROPS),
            Map.entry("/actuator/axile-details", DefaultAuthority.DETAILS),
            Map.entry("/actuator/axile-env/**", DefaultAuthority.ENV),
            Map.entry("/actuator/heapdump", DefaultAuthority.HEAP_DUMP),
            Map.entry("/actuator/threaddump", DefaultAuthority.THREAD_DUMP),
            Map.entry("/actuator/axile-metrics**", DefaultAuthority.METRICS),
            Map.entry("/actuator/loggers**", DefaultAuthority.LOGGERS),
            Map.entry("/actuator/mappings", DefaultAuthority.MAPPINGS),
            Map.entry("/actuator/scheduledtasks**", DefaultAuthority.SCHEDULED_TASKS),
            Map.entry("/actuator/scheduled-tasks-management**", DefaultAuthority.SCHEDULED_TASKS_MANAGEMENT));

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Optional<Authority> resolve(String path) {
        return pathAuthoritiesMap.entrySet().stream()
                .filter(entry -> matcher.match(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
