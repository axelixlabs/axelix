package com.nucleonforge.axile.common.api.caches;

/**
 * The result of a cache cleared operation executed within a specific application instance.
 *
 * @param cleared indicates whether the cache (or a specific entry) was successfully cleared
 *
 * @since 06.10.2025
 * @author Nikita Kirillov
 */
public record CacheDispatcherClearResult(boolean cleared) {}
