package com.nucleonforge.axile.master.service.convert;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;
import com.nucleonforge.axile.master.api.response.caches.CacheDispatcherClearResponse;

/**
 * The {@link Converter} from {@link CacheDispatcherClearResult} to {@link CacheDispatcherClearResponse}
 *
 * @since 02.10.2025
 * @author Nikita Kirillov
 */
@Service
public class CacheDispatcherClearResultConverter
        implements Converter<CacheDispatcherClearResult, CacheDispatcherClearResponse> {

    @Override
    public @NonNull CacheDispatcherClearResponse convertInternal(@NonNull CacheDispatcherClearResult source) {
        return new CacheDispatcherClearResponse(source.cleared());
    }
}
