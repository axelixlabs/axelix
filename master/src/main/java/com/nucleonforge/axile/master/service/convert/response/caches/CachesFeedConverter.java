/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
