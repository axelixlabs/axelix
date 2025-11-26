package com.nucleonforge.axile.sbs.spring.cache;

/**
 * Dispatcher interface for executing cache operations
 * (such as evicting entries or clearing caches) across different CacheManager instances.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 */
public interface CacheDispatcher {

    /**
     * Clears the entire cache with the given name from the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache to clear
     * @return {@code true} if the cache was found and cleared; {@code false} if the manager or cache was not found
     */
    boolean clear(String cacheManagerName, String cacheName);

    /**
     * Evicts a specific key from the given cache managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache
     * @param key              the key to remove
     * @return {@code true} if the key existed in the cache and was removed;
     * {@code false} if the manager, cache, or key was not found
     */
    boolean clear(String cacheManagerName, String cacheName, Object key);

    /**
     * Clears all caches managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @return {@code true} if at least one cache was found and cleared;
     * {@code false} if the manager was not found or no caches could be cleared
     */
    boolean clearAll(String cacheManagerName);

    /**
     * Enables all caches in the specified cache manager by name.
     * This activates caching operations for all caches in the given cache manager.
     *
     * @param managerName the name of the cache manager to enable
     */
    void enableCacheManager(String managerName);

    /**
     * Disables all caches in the specified cache manager by name.
     * This deactivates caching operations for all caches in the given cache manager.
     *
     * @param managerName the name of the cache manager to disable
     */
    void disableCacheManager(String managerName);

    /**
     * Enables a specific cache within the specified cache manager.
     * This activates caching operations for the given cache only.
     *
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to enable
     */
    void enableCache(String managerName, String cacheName);

    /**
     * Disables a specific cache within the specified cache manager.
     * This deactivates caching operations for the given cache only.
     *
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to disable
     */
    void disableCache(String managerName, String cacheName);

    /**
     * Enable all cache managers in the application, activates caching functionality across all cache manager instances.
     */
    void enableAllCache();
}
