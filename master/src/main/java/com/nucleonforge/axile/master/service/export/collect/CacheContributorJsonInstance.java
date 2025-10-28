package com.nucleonforge.axile.master.service.export.collect;

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
public class CacheContributorJsonInstance extends AbstractJsonInstanceStateCollector {

    private final CachesReadApi cachesReadApi;

    public CacheContributorJsonInstance(final CachesReadApi cachesReadApi) {
        this.cachesReadApi = cachesReadApi;
    }

    @Override
    public String getName() {
        return "cache";
    }

    @Override
    protected Object collectInternal(String instanceId) {
        return cachesReadApi.getAllCaches(instanceId);
    }
}
