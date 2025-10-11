package com.nucleonforge.axile.master.api.response.caches;

/**
 * The profile contains details about the requested cache.
 *
 * @param name            The cache name.
 * @param target          The fully qualified name of the native cache.
 * @param cacheManager    The cache manager name.
 *
 * @author Sergey Cherkasov
 */
public record CacheProfileResponse(String name, String target, String cacheManager) {}
