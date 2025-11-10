package com.nucleonforge.axile.master.api.caches;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.common.domain.http.SingleValueQueryParameter;
import com.nucleonforge.axile.master.api.ApiPaths;
import com.nucleonforge.axile.master.api.response.caches.CacheProfileResponse;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.caches.GetAllCachesEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.GetCacheByNameEndpointProber;

/**
 * The API for managing caches. Endpoints for retrieving information about the application caches.
 *
 * @author Sergey Cherkasov
 */
@RestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesReadApi {

    private final GetAllCachesEndpointProber getAllCachesEndpointProber;
    private final GetCacheByNameEndpointProber getCacheByNameEndpointProber;
    private final Converter<ServiceCaches, CachesResponse> serviceCachesConverter;
    private final Converter<SingleCache, CacheProfileResponse> singleCacheConverter;

    public CachesReadApi(
            GetAllCachesEndpointProber getAllCachesEndpointProber,
            GetCacheByNameEndpointProber getCacheByNameEndpointProber,
            Converter<ServiceCaches, CachesResponse> serviceCachesConverter,
            Converter<SingleCache, CacheProfileResponse> singleCacheConverter) {
        this.getAllCachesEndpointProber = getAllCachesEndpointProber;
        this.getCacheByNameEndpointProber = getCacheByNameEndpointProber;
        this.serviceCachesConverter = serviceCachesConverter;
        this.singleCacheConverter = singleCacheConverter;
    }

    @GetMapping(path = ApiPaths.CachesApi.INSTANCE_ID)
    public CachesResponse getAllCaches(@PathVariable("instanceId") String instanceId) {
        ServiceCaches response = getAllCachesEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(serviceCachesConverter.convert(response));
    }

    @GetMapping(path = ApiPaths.CachesApi.CACHE_NAME)
    public CacheProfileResponse getCacheByNameWithQueryParameter(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {

        SingleValueQueryParameter queryParameter = new SingleValueQueryParameter("cacheManager", cacheManager);
        HttpPayload payload = new DefaultHttpPayload(List.of(queryParameter), Map.of("name", cacheName));
        SingleCache response = getCacheByNameEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(singleCacheConverter.convert(response));
    }
}
