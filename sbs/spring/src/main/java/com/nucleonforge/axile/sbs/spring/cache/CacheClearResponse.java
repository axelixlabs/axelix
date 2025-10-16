package com.nucleonforge.axile.sbs.spring.cache;

/**
 * Response wrapper that indicates the result of a cache clear operation.
 *
 * @param cleared {@code true} if the cache was successfully cleared, {@code false} otherwise.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 */
public record CacheClearResponse(boolean cleared) {}
