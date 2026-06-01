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

import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.mcp.auth.McpIdentityAccessManager;

/**
 * Caching decorator over the delegated {@link EndpointProber}.
 *
 * @author Abubakar Muradov
 * @author Mikhail Polivakha
 */
public class CachingEndpointProber<T> implements EndpointProber<T> {

    private final EndpointProber<T> delegate;
    private final Cache<CacheKey, T> cache;

    public CachingEndpointProber(EndpointProber<T> delegate) {
        this(delegate, defaultCache());
    }

    CachingEndpointProber(EndpointProber<T> delegate, Cache<CacheKey, T> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    private static <T> Cache<CacheKey, T> defaultCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.of(15, ChronoUnit.MINUTES))
                .maximumSize(256L)
                .softValues()
                .build();
    }

    @Override
    public @NonNull T invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException {
        return cache.get(
                new CacheKey(instanceId, httpPayload), key -> delegate.invoke(key.instanceId(), key.httpPayload()));
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

    /**
     * Caching key for the given Instance.
     * <p>
     * Now, the important note here is that the {@link SecurityContext} is not a part of the caching key.
     * In general, it needs to be, otherwise it is a security breach, but in our case, the access to {@link CachingEndpointProber}
     * is guarded by the {@link WebIdentityAccessManager} and {@link McpIdentityAccessManager} abstractions, so when the request
     * hits the cache, we can be sure, that the access is authorized.
     *
     * @implNote <strong>IMPORTANT NOTE:</strong> we cannot reliably cache Config-Props and env feed since that would cause potentially
     *           sensitive information to be exposed.
     */
    record CacheKey(InstanceId instanceId, HttpPayload httpPayload) {}
}
