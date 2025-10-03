package com.nucleonforge.axile.common.api.caches;

import org.jspecify.annotations.Nullable;

/**
 * Represents a request to clear a specific cache or a particular entry within it
 * in a target application instance.
 *
 * @param cacheName the name of the cache to clear. Must not be null.
 * @param key the specific key to be cleared from the cache. If {@code null}, the entire cache will be cleared.
 *
 * @since 06.10.2025
 * @author Nikita Kirillov
 */
public record CacheDispatcherClearRequest(String cacheName, @Nullable Object key) {}
