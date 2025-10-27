package com.nucleonforge.axile.master.service.state.export;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.caches.CachesReadApi;

/**
 * Collects Spring Caches information for application state export.
 *
 * @see CachesReadApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class CacheDataCollector implements StateDataCollector {

    private final CachesReadApi cachesReadApi;

    public CacheDataCollector(final CachesReadApi cachesReadApi) {
        this.cachesReadApi = cachesReadApi;
    }

    @Override
    public String getName() {
        return "cache";
    }

    @Override
    public Object collectData(String instanceId) {
        return cachesReadApi.getAllCaches(instanceId);
    }
}
