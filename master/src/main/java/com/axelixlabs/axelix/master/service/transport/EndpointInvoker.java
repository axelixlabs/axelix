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
import com.axelixlabs.axelix.master.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * Abstraction that is capable to invoke the Axelix endpoint on the given instance, given the particular
 * payload.
 *
 * @author Mikhail Polivakha
 */
public interface EndpointInvoker {

    /**
     * Invoke endpoint on instance with the given payload.
     *
     * @param instanceId the ID of the {@link Instance} on which the endpoint is supposed to be invoked.
     * @param endpoint the endpoint that should be invoked.
     * @param httpPayload the HTTP payload (headers, body etc.) to be sent.
     * @return the value returned by endpoint.
     *
     * @throws EndpointInvocationException in case the invocation to managed service did not result in successful response.
     * @throws BadRequestException in case the managed service cannot process the request.
     * @throws InstanceNotFoundException in case the instance with the given ID is not known to this {@link EndpointInvoker}.
     * @throws ClassCastException in case the response from actuator endpoint cannot be cast to the requested type {@code <O>}.
     *
     * @param <O> the type of the returned value by the endpoint.
     */
    <O> O invoke(InstanceId instanceId, ActuatorEndpoint endpoint, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException, ClassCastException;

    /**
     * Invoke endpoint on instance with the given payload. Similar to {@link #invoke(InstanceId, ActuatorEndpoint, HttpPayload)},
     * but this invocation is not expected to result in any value.
     *
     * @param instanceId the ID of the {@link Instance} on which the endpoint is supposed to be invoked.
     * @param endpoint the endpoint that should be invoked.
     * @param httpPayload the HTTP payload (headers, body etc.) to be sent.
     *
     * @throws EndpointInvocationException in case the invocation to managed service did not result in successful response.
     * @throws BadRequestException in case the managed service cannot process the request.
     * @throws InstanceNotFoundException in case the instance with the given ID is not known to this {@link EndpointInvoker}.
     */
    void invokeNoValue(InstanceId instanceId, ActuatorEndpoint endpoint, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException;
}
