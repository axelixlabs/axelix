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
package com.axelixlabs.axelix.master.service.auth;

import java.util.Collection;
import java.util.Optional;

import org.springframework.http.server.PathContainer;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * Resolves the {@link MasterWebEndpoint} that was attempted to be accessed
 * from the {@code (path, method)} pair.
 *
 * @author Mikhail Polivakha
 */
public class MasterWebEndpointResolver {

    private final Collection<MasterWebEndpoint> endpoints;

    /**
     * Creates a resolver backed by the given set of endpoints (e.g. the built-in catalog plus endpoints
     * contributed by other modules such as the enterprise distribution).
     */
    public MasterWebEndpointResolver(Collection<MasterWebEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Resolves the endpoint addressed by the given relative path and HTTP method.
     *
     * @param relativeRequestPath the prefix-stripped relative path (e.g. {@code /env/feed/{instanceId}}).
     * @param httpMethod          the HTTP method of the request.
     *
     * @return the matching {@link MasterWebEndpoint}, or {@link Optional#empty()} if none matches.
     */
    public Optional<MasterWebEndpoint> resolveEndpoint(String relativeRequestPath, HttpMethod httpMethod) {

        PathContainer pathContainer = PathContainer.parsePath(relativeRequestPath);

        for (MasterWebEndpoint endpoint : endpoints) {
            if (endpoint.method() == httpMethod && endpoint.path().matches(pathContainer)) {
                return Optional.of(endpoint);
            }
        }

        return Optional.empty();
    }
}
