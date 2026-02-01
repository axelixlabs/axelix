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

import com.axelixlabs.axelix.master.domain.ActuatorEndpoint;
import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * The core service that is responsible to probe certain information from discovered services.
 * <p>
 *
 * @param <O> the type of the response body (output).
 * @author Mikhail Polivakha
 */
public interface EndpointProber<O> {

    /**
     * Invoke the actual {@link ActuatorEndpoint} on the managed service.
     *
     * @param instanceId the id of the instance on which the endpoint should be invoked.
     * @param httpPayload the abstraction that encapsulates the http payload of the request
     * @return the result of the invocation. Guaranteed to be not null.
     * @throws EndpointInvocationException in case the invocation to managed service did not result in successful response.
     * @throws BadRequestException in case the managed service cannot process the request.
     * @throws InstanceNotFoundException in case the instance with the given ID is not present.
     */
    @NonNull
    O invoke(@NonNull InstanceId instanceId, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException;

    /**
     * Invoke the actual {@link ActuatorEndpoint} using the given base url.
     *
     * @param baseUrl the base url of the request
     * @param httpPayload the abstraction that encapsulates the http payload of the request
     * @return the result of the invocation. Guaranteed to be not null.
     * @throws EndpointInvocationException in case the invocation to managed service did not result in successful response.
     * @throws BadRequestException in case the managed service cannot process the request.
     */
    @NonNull
    O invoke(@NonNull String baseUrl, HttpPayload httpPayload) throws EndpointInvocationException, BadRequestException;

    /**
     * @return the {@link ActuatorEndpoint} that this prober supports
     */
    @NonNull
    ActuatorEndpoint supports();
}
