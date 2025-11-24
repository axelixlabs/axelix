package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.caches.CachesReadApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.CachesStateComponentSettings;

/**
 * Collects Spring Caches information for application state export.
 *
 * @see CachesReadApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class CacheContributorJsonInstance extends AbstractJsonInstanceStateCollector<CachesStateComponentSettings> {

    private final CachesReadApi cachesReadApi;

    public CacheContributorJsonInstance(final CachesReadApi cachesReadApi) {
        this.cachesReadApi = cachesReadApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CACHES;
    }

    @Override
    protected Object collectInternal(String instanceId, CachesStateComponentSettings settings) {
        return cachesReadApi.getAllCaches(instanceId);
    }
}
