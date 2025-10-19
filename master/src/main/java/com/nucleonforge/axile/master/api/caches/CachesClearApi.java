package com.nucleonforge.axile.master.api.caches;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.common.domain.http.SingleValueQueryParameter;
import com.nucleonforge.axile.master.api.ApiPaths;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.transport.caches.ClearAllCachesEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.ClearCacheByNameEndpointProber;

/**
 * The API for managing caches. Endpoints for clearing the application caches.
 *
 * @author Sergey Cherkasov
 */
@RestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesClearApi {

    private final ClearAllCachesEndpointProber clearAllCachesEndpointProber;
    private final ClearCacheByNameEndpointProber clearCacheByNameEndpointProber;

    public CachesClearApi(
            ClearAllCachesEndpointProber clearAllCachesEndpointProber,
            ClearCacheByNameEndpointProber clearCacheByNameEndpointProber) {
        this.clearAllCachesEndpointProber = clearAllCachesEndpointProber;
        this.clearCacheByNameEndpointProber = clearCacheByNameEndpointProber;
    }

    @DeleteMapping(path = ApiPaths.CachesApi.INSTANCE_ID)
    public void clearAllCaches(@PathVariable("instanceId") String instanceId) {
        clearAllCachesEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }

    @DeleteMapping(path = ApiPaths.CachesApi.CACHE_NAME)
    public void clearCacheByNameWithQueryParameter(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {

        SingleValueQueryParameter queryParameter = new SingleValueQueryParameter("cacheManager", cacheManager);
        HttpPayload payload = new DefaultHttpPayload(List.of(queryParameter), Map.of("name", cacheName));
        clearCacheByNameEndpointProber.invoke(InstanceId.of(instanceId), payload);
    }
}
