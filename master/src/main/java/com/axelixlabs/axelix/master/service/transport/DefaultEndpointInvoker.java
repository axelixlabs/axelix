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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Default {@link EndpointInvoker} that delegates the actual query execution to selected {@link EndpointProber}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@Component
public class DefaultEndpointInvoker implements EndpointInvoker {

    private static final Logger log = LoggerFactory.getLogger(DefaultEndpointInvoker.class);

    private final Map<ActuatorEndpoint, EndpointProber<?>> endpointProbers;

    private final InstanceRegistry instanceRegistry;

    public DefaultEndpointInvoker(List<EndpointProber<?>> endpointProbers, InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
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

    @Override
    public void invokeNoValueForInstances(List<String> instanceIds, ActuatorEndpoint endpoint, HttpPayload payload)
            throws PartiallyUpdatedException {

        List<InstanceId> ids = instanceIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .map(InstanceId::of)
                .toList();

        EndpointProber<?> prober = getEndpointProber(endpoint);
        List<Instance> instances = instanceRegistry.getAllByIds(ids);

        if (instances.size() != ids.size()) {
            Set<InstanceId> foundIds = instances.stream().map(Instance::id).collect(Collectors.toSet());
            List<InstanceId> missingIds =
                    ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new InstanceNotFoundException(missingIds);
        }

        if (instances.stream().map(Instance::applicationId).distinct().count() > 1) {
            throw new BadRequestException("All instances must belong to the same application");
        }

        List<String> failedInstanceIds = new ArrayList<>();

        for (Instance instance : instances) {
            try {
                prober.invoke(instance.actuatorUrl(), payload);
            } catch (EndpointInvocationException | BadRequestException e) {
                failedInstanceIds.add(instance.id().instanceId());
            }
        }

        if (!failedInstanceIds.isEmpty()) {
            throw new PartiallyUpdatedException(failedInstanceIds);
        }
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
