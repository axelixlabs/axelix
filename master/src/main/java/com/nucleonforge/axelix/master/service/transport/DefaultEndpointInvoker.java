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
package com.nucleonforge.axelix.master.service.transport;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.InstanceId;

/**
 * Default {@link EndpointInvoker} that delegates the actual query execution to selected {@link EndpointProber}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultEndpointInvoker implements EndpointInvoker {

    private static final Logger log = LoggerFactory.getLogger(DefaultEndpointInvoker.class);

    private final Map<ActuatorEndpoint, EndpointProber<?>> endpointProbers;

    public DefaultEndpointInvoker(List<EndpointProber<?>> endpointProbers) {
        this.endpointProbers =
                endpointProbers.stream().collect(Collectors.toMap(EndpointProber::supports, Function.identity()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> O invoke(InstanceId instanceId, ActuatorEndpoint endpoint, HttpPayload httpPayload) {
        EndpointProber<?> prober = getEndpointProber(endpoint);

        Object result = prober.invoke(instanceId, httpPayload);

        try {
            return (O) result;
        } catch (ClassCastException e) {
            log.error("[BUG] Unable to cast {} to requested type. Please, report to maintainers", result);
            throw e;
        }
    }

    @Override
    public void invokeNoValue(InstanceId instanceId, ActuatorEndpoint endpoint, HttpPayload httpPayload)
            throws EndpointInvocationException, BadRequestException, InstanceNotFoundException {
        getEndpointProber(endpoint).invoke(instanceId, httpPayload);
    }

    @NonNull
    private EndpointProber<?> getEndpointProber(ActuatorEndpoint endpoint) {
        EndpointProber<?> prober = endpointProbers.get(endpoint);

        if (prober == null) {
            throw new EndpointInvocationException("Attempted to invoke an unknown actuator endpoint");
        }
        return prober;
    }
}
