/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.convert.response.caches;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.master.api.response.caches.CachesResponse;
import com.axelixlabs.axelix.master.api.response.caches.CachesResponse.CacheManagers;
import com.axelixlabs.axelix.master.api.response.caches.CachesResponse.Caches;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link CachesFeed} to {@link CachesResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class CachesFeedConverter implements Converter<CachesFeed, CachesResponse> {

    @Override
    public @NonNull CachesResponse convertInternal(@NonNull CachesFeed source) {
        if (!source.getCacheManagers().isEmpty()) {
            return new CachesResponse(convertCacheManager(source));
        }

        return new CachesResponse();
    }

    private List<CacheManagers> convertCacheManager(CachesFeed source) {
        return source.getCacheManagers().stream()
                .map(cm -> new CacheManagers(cm.getName(), convertCache(cm.getCaches())))
                .toList();
    }

    private List<Caches> convertCache(List<CachesFeed.Cache> caches) {
        return caches.stream()
                .map(c -> new Caches(
                        c.getName(),
                        c.getTarget(),
                        c.getHitsCount(),
                        c.getMissesCount(),
                        c.getEstimatedEntrySize(),
                        c.isEnabled()))
                .toList();
    }
}
