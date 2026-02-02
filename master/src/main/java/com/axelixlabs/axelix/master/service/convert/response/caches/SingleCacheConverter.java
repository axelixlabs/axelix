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

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.caches.SingleCache;
import com.axelixlabs.axelix.master.api.response.caches.CacheProfileResponse;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link SingleCache} to {@link CacheProfileResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class SingleCacheConverter implements Converter<SingleCache, CacheProfileResponse> {

    @Override
    public @NonNull CacheProfileResponse convertInternal(@NonNull SingleCache source) {
        return new CacheProfileResponse(source.getName(), source.getTarget(), source.getCacheManager());
    }
}
