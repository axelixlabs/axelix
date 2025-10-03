package com.nucleonforge.axile.spring.cache;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;

/**
 * Central component responsible for dispatching
 * cache-related operations to the appropriate {@link CacheManager} based on its name.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 */
public class DefaultCacheDispatcher implements CacheDispatcher {

    private final Map<String, CacheManagerAdapter> adapters;

    public DefaultCacheDispatcher(Map<String, CacheManager> managers) {
        this.adapters = managers.entrySet().stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey, entry -> new DefaultCacheManagerAdapter(entry.getValue())));
    }

    @Override
    public boolean clear(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null && adapter.clear(cacheName);
    }

    @Override
    public boolean clear(String cacheManagerName, String cacheName, Object key) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null && adapter.clear(cacheName, key);
    }

    @Override
    public boolean clearAll(String cacheManagerName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null && adapter.clearAll();
    }
}
