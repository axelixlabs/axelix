package com.nucleonforge.axile.spring.cache;

import org.springframework.cache.CacheManager;

/**
 * Adapter to Spring's {@link CacheManager}.
 *
 * <p>
 * Provides a unified interface for performing cache clear operations.
 * All methods return {@code true} if at least one entry was cleared,
 * and {@code false} if no entries were cleared.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public interface CacheManagerAdapter {

    /**
     * Clear the entire cache with the given name.
     *
     * @param cacheName the name of the cache to clear
     * @return {@code true} if the cache existed and was cleared; {@code false} otherwise
     */
    boolean clear(String cacheName);

    /**
     * Clear a specific entry in the specified cache.
     *
     * @param cacheName the name of the cache
     * @param key       the key to clear
     * @return {@code true} if the key existed in the cache and was cleared; {@code false} otherwise
     */
    boolean clear(String cacheName, Object key);

    /**
     * Clear all caches managed by this {@link CacheManager}.
     *
     * @return {@code true} if at least one cache was cleared; {@code false} otherwise
     */
    boolean clearAll();
}
