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

import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import org.jspecify.annotations.NonNull;

import java.util.function.BiFunction;

/**
 * Caching decorator over the actual {@link ProxyingEndpointProber}.
 *
 * @author Abubakar Muradov
 */
public class CachedProxyingEndpointProber implements EndpointProber<byte[]> {

    private final ProxyingEndpointProber delegate;
    private final Lazy<BiFunction<InstanceId, HttpPayload, byte[]>> lazyInstanceId;
    private final Lazy<BiFunction<String, HttpPayload, byte[]>> lazyBaseUrl;

    public CachedProxyingEndpointProber(ProxyingEndpointProber delegate) {
        this.delegate = delegate;
        this.lazyInstanceId = Lazy.of(() -> delegate::invoke);
        this.lazyBaseUrl = Lazy.of(() -> delegate::invoke);
    }

    @Override
    public byte @NonNull [] invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload
    ) throws EndpointInvocationException, BadRequestException, InstanceNotFoundException {
        return lazyInstanceId.get().apply(instanceId, httpPayload);
    }

    @Override
    public byte @NonNull [] invoke(@NonNull String baseUrl, HttpPayload httpPayload
    ) throws EndpointInvocationException, BadRequestException {
        return lazyBaseUrl.get().apply(baseUrl, httpPayload);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return delegate.supports();
    }
}
