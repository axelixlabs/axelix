package com.nucleonforge.axile.master.service.convert.response.caches;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.CachesFeed;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse.CacheManagers;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse.Caches;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link CachesFeed} to {@link CachesResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class CachesFeedConverter implements Converter<CachesFeed, CachesResponse> {

    @Override
    public @NonNull CachesResponse convertInternal(@NonNull CachesFeed source) {
        if (!source.cacheManagers().isEmpty()) {
            return new CachesResponse(convertCacheManager(source));
        }

        return new CachesResponse();
    }

    private List<CacheManagers> convertCacheManager(CachesFeed source) {
        return source.cacheManagers().stream()
                .map(cm -> new CacheManagers(cm.name(), convertCache(cm.caches())))
                .toList();
    }

    private List<Caches> convertCache(List<CachesFeed.Caches> caches) {
        return caches.stream()
                .map(c -> new Caches(c.name(), c.target(), c.enabled()))
                .toList();
    }
}
