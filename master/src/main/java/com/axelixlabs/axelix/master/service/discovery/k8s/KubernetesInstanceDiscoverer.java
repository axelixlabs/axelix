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
package com.axelixlabs.axelix.master.service.discovery.k8s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.InstanceFactory;
import com.axelixlabs.axelix.master.service.discovery.AbstractInstancesDiscoverer;
import com.axelixlabs.axelix.master.service.discovery.InstancesDiscoverer;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Kubernetes implementation of {@link InstancesDiscoverer}.
 * <p>This service discovers running instances of services registered
 * in a Kubernetes cluster.</p>
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
// TODO:
//  We're loosing the need for concrete InstancesDiscoverers. We need to
//  re-consider this abstraction in general.
public class KubernetesInstanceDiscoverer extends AbstractInstancesDiscoverer {

    private static final Logger log = LoggerFactory.getLogger(KubernetesInstanceDiscoverer.class);

    private final InstanceFactory instanceFactory;

    public KubernetesInstanceDiscoverer(
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            InstanceFactory instanceFactory) {
        super(log, discoveryClient, managedServiceMetadataEndpointProber, axelixVersionDiscoverer);
        this.instanceFactory = instanceFactory;
    }

    @Override
    protected Instance toDomainInstance(InstanceIntermediateProfile profile) throws IllegalArgumentException {
        ServiceInstance serviceInstance = profile.serviceInstance();

        if (serviceInstance instanceof KubernetesServiceInstance k8sInstance) {
            return instanceFactory.createInstance(
                    k8sInstance.getInstanceId(),
                    k8sInstance.podName(),
                    k8sInstance.getDeploymentAt(),
                    serviceInstance.getUri() + ACTUATOR_ENDPOINT_POSTFIX,
                    profile.metadata());
        } else {
            throw new IllegalArgumentException(buildErrorMessage(serviceInstance));
        }
    }

    private static String buildErrorMessage(ServiceInstance serviceInstance) {
        return "Unable to register K8S pod '%s' as a managed instance - expected %s to be an instance of %s, but actually is %s"
                .formatted(
                        serviceInstance.getInstanceId(),
                        ServiceInstance.class.getSimpleName(),
                        org.springframework.cloud.kubernetes.commons.discovery.KubernetesServiceInstance.class
                                .getName(),
                        serviceInstance.getClass().getName());
    }
}
