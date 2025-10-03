package com.nucleonforge.axile.spring.cache;

/**
 * Dispatcher interface for executing cache operations
 * (such as clearing entries or clearing caches) across different CacheManager instances.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 */
public interface CacheDispatcher {

    /**
     * Clear the entire cache with the given name from the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache to clear
     * @return {@code true} if the cache was found and cleared; {@code false} if the manager or cache was not found
     */
    boolean clear(String cacheManagerName, String cacheName);

    /**
     * Clear a specific key from the given cache managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache
     * @param key              the key to clear
     * @return {@code true} if the key existed in the cache and was cleared;
     * {@code false} if the manager, cache, or key was not found
     */
    boolean clear(String cacheManagerName, String cacheName, Object key);

    /**
     * Clear all caches managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @return {@code true} if at least one cache was found and cleared;
     * {@code false} if the manager was not found or no caches could be cleared
     */
    boolean clearAll(String cacheManagerName);
}
