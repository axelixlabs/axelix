package com.nucleonforge.axile.master.api.response.caches;

import java.util.Collections;
import java.util.List;

/**
 * The profile contains a list of all cache managers in the application.
 *
 * @param cacheManagers    The list of cache managers.
 *
 * @author Sergey Cherkasov
 */
public record CachesResponse(List<CacheManagers> cacheManagers) {

    public CachesResponse() {
        this(Collections.emptyList());
    }

    /**
     * The profile contains a list of all caches in the application.
     *
     * @param name   The cache manager name.
     * @param caches The list of caches.
     *
     * @author Sergey Cherkasov
     */
    public record CacheManagers(String name, List<Caches> caches) {}

    /**
     * The profile contains details about the cache.
     *
     * @param name    The cache name.
     * @param target  The fully qualified name of the native cache.
     * @param enabled Whether the cache is enabled ({@code true}) or disabled ({@code false}).
     *
     * @author Sergey Cherkasov
     */
    public record Caches(String name, String target, boolean enabled) {}
}
