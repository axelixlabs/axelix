package com.nucleonforge.axile.sbs.auth.filter;

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
            Map.entry("/actuator/cache-dispatcher/**", DefaultAuthority.CACHE_DISPATCHER),
            Map.entry("/actuator/property-management/**", DefaultAuthority.PROPERTY_MANAGEMENT),
            Map.entry("/actuator/profile-management/**", DefaultAuthority.PROFILE_MANAGEMENT),
            Map.entry("/actuator/beans", DefaultAuthority.BEANS),
            Map.entry("/actuator/caches/**", DefaultAuthority.CACHES),
            Map.entry("/actuator/health/**", DefaultAuthority.HEALTH),
            Map.entry("/actuator/info", DefaultAuthority.INFO),
            Map.entry("/actuator/conditions", DefaultAuthority.CONDITIONS),
            Map.entry("/actuator/env**", DefaultAuthority.ENV),
            Map.entry("/actuator/heapdump", DefaultAuthority.HEAP_DUMP),
            Map.entry("/actuator/threaddump", DefaultAuthority.THREAD_DUMP),
            Map.entry("/actuator/metrics/**", DefaultAuthority.METRICS),
            Map.entry("/actuator/mappings", DefaultAuthority.MAPPINGS));

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Optional<Authority> resolve(String path) {
        return pathAuthoritiesMap.entrySet().stream()
                .filter(entry -> matcher.match(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
