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
package com.axelixlabs.axelix.master.service.transport;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

/**
 * Caching decorator over the delegated {@link EndpointProber}.
 *
 * @author Abubakar Muradov
 * @author Mikhail Polivakha
 */
public class CachingEndpointProber<T> implements EndpointProber<T> {

    private final EndpointProber<T> delegate;
    private final Cache<InstanceId, T> cache;

    public CachingEndpointProber(EndpointProber<T> delegate) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.of(15, ChronoUnit.MINUTES))
                .maximumSize(256L)
                .softValues()
                .build();
    }

    @Override
    public @NonNull T invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException {
        return cache.get(instanceId, key -> delegate.invoke(key, httpPayload));
    }

    @Override
    public @NonNull T invoke(@NonNull String baseUrl, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException {
        return delegate.invoke(baseUrl, httpPayload);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return delegate.supports();
    }
}
