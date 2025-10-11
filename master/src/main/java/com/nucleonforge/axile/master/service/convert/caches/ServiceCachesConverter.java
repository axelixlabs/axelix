package com.nucleonforge.axile.master.service.convert.caches;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link ServiceCaches} to {@link CachesResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ServiceCachesConverter implements Converter<ServiceCaches, CachesResponse> {

    @Override
    public @NonNull CachesResponse convertInternal(@NonNull ServiceCaches source) {
        if (!source.cacheManagers().isEmpty()) {
            return new CachesResponse(convertCacheManager(source));
        }

        return new CachesResponse();
    }

    private List<CachesResponse.CacheManagers> convertCacheManager(ServiceCaches source) {
        return source.cacheManagers().entrySet().stream()
                .map(cm -> new CachesResponse.CacheManagers(cm.getKey(), convertCache(cm.getValue())))
                .toList();
    }

    private List<CachesResponse.CacheManagers.Caches> convertCache(ServiceCaches.CacheManagers cacheManagers) {
        return cacheManagers.caches().entrySet().stream()
                .map(c -> new CachesResponse.CacheManagers.Caches(
                        c.getKey(), c.getValue().target()))
                .toList();
    }
}
