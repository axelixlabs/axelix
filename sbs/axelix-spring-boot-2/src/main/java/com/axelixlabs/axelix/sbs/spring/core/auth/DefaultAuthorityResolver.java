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
import com.axelixlabs.axelix.common.auth.core.GlobalAuthority;

/**
 * Default implementation of {@link AuthorityResolver}.
 *
 * @see GlobalAuthority
 * @see AntPathMatcher
 * @since 29.07.2025
 * @author Nikita Kirillov
 */
public class DefaultAuthorityResolver implements AuthorityResolver {

    private final Map<String, Authority> pathAuthoritiesMap = Map.ofEntries(
            Map.entry("/actuator/axelix-beans", GlobalAuthority.BEANS),
            Map.entry("/actuator/axelix-caches/**", GlobalAuthority.CACHES),
            Map.entry("/actuator/cache-dispatcher/**", GlobalAuthority.CACHE_DISPATCHER),
            Map.entry("/actuator/property-management/**", GlobalAuthority.PROPERTY_MANAGEMENT),
            Map.entry("/actuator/profile-management/**", GlobalAuthority.PROFILE_MANAGEMENT),
            Map.entry("/actuator/health/**", GlobalAuthority.HEALTH),
            Map.entry("/actuator/info", GlobalAuthority.INFO),
            Map.entry("/actuator/axelix-conditions", GlobalAuthority.CONDITIONS),
            Map.entry("/actuator/axelix-configprops", GlobalAuthority.CONFIGPROPS),
            Map.entry("/actuator/axelix-details", GlobalAuthority.DETAILS),
            Map.entry("/actuator/axelix-env/**", GlobalAuthority.ENV),
            Map.entry("/actuator/heapdump", GlobalAuthority.HEAP_DUMP),
            Map.entry("/actuator/axelix-thread-dump/**", GlobalAuthority.THREAD_DUMP),
            Map.entry("/actuator/axelix-metrics/**", GlobalAuthority.METRICS),
            Map.entry("/actuator/loggers/**", GlobalAuthority.LOGGERS),
            Map.entry("/actuator/mappings", GlobalAuthority.MAPPINGS),
            Map.entry("/actuator/axelix-scheduled-tasks/**", GlobalAuthority.SCHEDULED_TASKS));

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Optional<Authority> resolve(String path) {
        return pathAuthoritiesMap.entrySet().stream()
                .filter(entry -> matcher.match(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
