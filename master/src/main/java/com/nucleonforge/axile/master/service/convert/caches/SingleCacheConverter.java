package com.nucleonforge.axile.master.service.convert.caches;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.master.api.response.caches.CacheProfileResponse;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link SingleCache} to {@link CacheProfileResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class SingleCacheConverter implements Converter<SingleCache, CacheProfileResponse> {

    @Override
    public @NonNull CacheProfileResponse convertInternal(@NonNull SingleCache source) {
        return new CacheProfileResponse(source.name(), source.target(), source.cacheManager());
    }
}
