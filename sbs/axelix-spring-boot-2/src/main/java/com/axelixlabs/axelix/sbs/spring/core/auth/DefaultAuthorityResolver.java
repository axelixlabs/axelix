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

import java.util.Map;
import java.util.Optional;

import org.springframework.util.AntPathMatcher;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;

/**
 * Default implementation of {@link AuthorityResolver}.
 *
 * @see ExternalAuthority
 * @see AntPathMatcher
 * @since 29.07.2025
 * @author Nikita Kirillov
 */
public class DefaultAuthorityResolver implements AuthorityResolver {

    private final Map<String, Authority> pathAuthoritiesMap = Map.ofEntries(
            Map.entry("/actuator/axelix-beans", ExternalAuthority.BEANS),
            Map.entry("/actuator/axelix-caches/**", ExternalAuthority.CACHES),
            Map.entry("/actuator/cache-dispatcher/**", ExternalAuthority.CACHE_DISPATCHER),
            Map.entry("/actuator/property-management/**", ExternalAuthority.PROPERTY_MANAGEMENT),
            Map.entry("/actuator/profile-management/**", ExternalAuthority.PROFILE_MANAGEMENT),
            Map.entry("/actuator/health/**", ExternalAuthority.HEALTH),
            Map.entry("/actuator/info", ExternalAuthority.INFO),
            Map.entry("/actuator/axelix-conditions", ExternalAuthority.CONDITIONS),
            Map.entry("/actuator/axelix-configprops", ExternalAuthority.CONFIGPROPS),
            Map.entry("/actuator/axelix-details", ExternalAuthority.DETAILS),
            Map.entry("/actuator/axelix-env/**", ExternalAuthority.ENV),
            Map.entry("/actuator/heapdump", ExternalAuthority.HEAP_DUMP),
            Map.entry("/actuator/axelix-thread-dump/**", ExternalAuthority.THREAD_DUMP),
            Map.entry("/actuator/axelix-metrics/**", ExternalAuthority.METRICS),
            Map.entry("/actuator/loggers/**", ExternalAuthority.LOGGERS),
            Map.entry("/actuator/mappings", ExternalAuthority.MAPPINGS),
            Map.entry("/actuator/axelix-scheduled-tasks/**", ExternalAuthority.SCHEDULED_TASKS));

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Optional<Authority> resolve(String path) {
        return pathAuthoritiesMap.entrySet().stream()
                .filter(entry -> matcher.match(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
