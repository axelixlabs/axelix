package com.nucleonforge.axile.sbs.spring.cache;

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

    @Override
    public void enableCacheManager(String cacheManagerName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.enableCacheManager();
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot enable cache manager.", cacheManagerName));
        }
    }

    @Override
    public void disableCacheManager(String cacheManagerName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.disableCacheManager();
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot disable cache manager.", cacheManagerName));
        }
    }

    @Override
    public void enableCache(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.enableCache(cacheName);
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot enable cache '%s'.",
                    cacheManagerName, cacheName));
        }
    }

    @Override
    public void disableCache(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.disableCache(cacheName);
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot disable cache '%s'.",
                    cacheManagerName, cacheName));
        }
    }

    @Override
    public boolean isCacheEnabled(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            return adapter.isCacheEnabled(cacheName);
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot check enabled status for cache '%s'.",
                    cacheManagerName, cacheName));
        }
    }
}
