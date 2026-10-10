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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.ProbeState;
import com.axelixlabs.axelix.master.domain.ProbeState.InstanceKey;
import com.axelixlabs.axelix.master.service.InstanceFactory;
import com.axelixlabs.axelix.master.service.discovery.CompatibilityDetectionStrategy;
import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile;
import com.axelixlabs.axelix.master.service.discovery.DiscoveryResult;
import com.axelixlabs.axelix.master.service.discovery.InstancesDiscoverer;
import com.axelixlabs.axelix.master.service.discovery.probe.ProbeOutcome;
import com.axelixlabs.axelix.master.service.discovery.probe.ProbeStateService;
import com.axelixlabs.axelix.master.service.discovery.probe.backoff.ProbeBackoff;
import com.axelixlabs.axelix.master.service.discovery.probe.backoff.ProbeBackoffProperties;
import com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdict;
import com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdicts;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Kubernetes implementation of {@link InstancesDiscoverer}. Every instance is probed through its
 * {@link com.axelixlabs.axelix.common.domain.ActuatorEndpoints#METADATA metadata endpoint}, with a per-instance
 * backoff that depends on the outcome of the previous probes.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Marsel Semenov
 */
public class KubernetesInstanceDiscoverer implements InstancesDiscoverer {

    // TODO:
    //  So, the problem is that the /actuator path is not guaranteed to be
    //  the path under which the actuator endpoints are going to be exposed.
    //  It is possible to override it via specific properties, see the doc
    //
    // https://docs.spring.io/spring-boot/docs/2.1.7.RELEASE/reference/html/production-ready-monitoring.html#production-ready-customizing-management-server-context-path
    //  So, we have to take this into account. It is however unclear how
    //  we can do that in case of automatic discovery.
    private static final String ACTUATOR_ENDPOINT_POSTFIX = "/actuator";

    private static final Logger logger = LoggerFactory.getLogger(KubernetesInstanceDiscoverer.class);

    private final InstanceFactory instanceFactory;
    private final DiscoveryClient discoveryClient;
    private final ManagedServiceMetadataEndpointProber managedServiceProber;
    private final CompatibilityDetectionStrategy compatibilityDetectionStrategy;
    private final ProbeStateService probeStateService;
    private final ProbeBackoff probeBackoff;
    private final ProbeBackoffProperties properties;

    public KubernetesInstanceDiscoverer(
            InstanceFactory instanceFactory,
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceProber,
            CompatibilityDetectionStrategy compatibilityDetectionStrategy,
            ProbeStateService probeStateService,
            ProbeBackoff probeBackoff,
            ProbeBackoffProperties properties) {
        this.instanceFactory = instanceFactory;
        this.discoveryClient = discoveryClient;
        this.managedServiceProber = managedServiceProber;
        this.compatibilityDetectionStrategy = compatibilityDetectionStrategy;
        this.probeStateService = probeStateService;
        this.probeBackoff = probeBackoff;
        this.properties = properties;
    }

    @Override
    public DiscoveryResult discover() {
        Instant now = Instant.now();

        Map<InstanceKey, ServiceInstance> visible = collectVisibleInstances();
        if (visible.isEmpty()) {
            return DiscoveryResult.EMPTY;
        }

        Map<InstanceKey, ProbeState> claimed = probeStateService.registerAndClaimDue(visible.keySet(), now);

        Map<InstanceKey, DiscoveredInstanceProfile> fresh = new HashMap<>();
        Set<InstanceKey> retained = new HashSet<>();
        List<ProbeState> results = new ArrayList<>();

        visible.forEach((key, serviceInstance) -> {
            ProbeState state = claimed.get(key);

            if (state == null) {
                // not due yet, or claimed by another replica
                retained.add(key);
                return;
            }

            ProbeOutcome outcome = probeSafely(key, serviceInstance, now);
            int failures = outcome.verdict().isFailure() ? state.consecutiveFailures() + 1 : 0;
            Instant nextAttempt = probeBackoff.nextAttempt(outcome, failures, now);
            Integer status = outcome instanceof ProbeOutcome.Failure f ? f.status() : null;

            results.add(state.withResult(outcome.verdict(), failures, status, nextAttempt, now));

            switch (outcome) {
                case ProbeOutcome.Success success -> fresh.put(key, success.profile());
                case ProbeOutcome.Failure failure -> {
                    logFailure(key, failure, failures);
                    if (failure.verdict().keepsRegistration(failures, properties.maxTransientFailures())) {
                        retained.add(key);
                    }
                }
            }
        });

        probeStateService.recordAll(results);
        return new DiscoveryResult(fresh, retained);
    }

    private Map<InstanceKey, ServiceInstance> collectVisibleInstances() {
        List<String> serviceIds = discoveryClient.getServices();
        if (CollectionUtils.isEmpty(serviceIds)) {
            return Map.of();
        }

        Map<InstanceKey, ServiceInstance> visible = new LinkedHashMap<>();
        for (String serviceId : serviceIds) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            if (CollectionUtils.isEmpty(instances)) {
                continue;
            }
            instances.forEach(instance ->
                    visible.putIfAbsent(new InstanceKey(serviceId, instance.getHost(), instance.getPort()), instance));
        }

        return visible;
    }

    /**
     * Never throws: an exception escaping the loop would lose the results of the whole run
     * and leave every claimed instance leased until the lease expires.
     */
    private ProbeOutcome probeSafely(InstanceKey key, ServiceInstance serviceInstance, Instant now) {
        try {
            return probe(key, serviceInstance, now);
        } catch (RuntimeException e) {
            logger.debug("Unexpected error while probing {}", serviceInstance.getInstanceId(), e);
            return new ProbeOutcome.Failure(ProbeVerdict.TRANSIENT, null, null, "unexpected error: " + e);
        }
    }

    private ProbeOutcome probe(InstanceKey key, ServiceInstance serviceInstance, Instant now) {
        String actuatorUrl = serviceInstance.getUri() + ACTUATOR_ENDPOINT_POSTFIX;

        BasicRegistrationMetadata metadata;
        try {
            metadata = managedServiceProber.invoke(actuatorUrl, NoHttpPayload.INSTANCE);
        } catch (BadRequestException e) {
            return new ProbeOutcome.Failure(ProbeVerdict.PERMANENT, 400, null, String.valueOf(e.getMessage()));
        } catch (EndpointInvocationException e) {
            Integer status = e.getStatusCode();
            ProbeVerdict verdict = ProbeVerdicts.fromStatus(status);
            Duration retryAfter =
                    verdict == ProbeVerdict.THROTTLED ? ProbeVerdicts.parseRetryAfter(e.getRetryAfter(), now) : null;
            return new ProbeOutcome.Failure(verdict, status, retryAfter, String.valueOf(e.getMessage()));
        }

        if (!compatibilityDetectionStrategy.isCompatible(metadata.getVersion())) {
            return rejected("Axelix starter version " + metadata.getVersion() + " is not supported by this Master");
        }

        // groupId is optional (Gradle services may omit it)
        if (!StringUtils.hasText(metadata.getArtifactId())) {
            return rejected("no valid application id (artifactId is mandatory)");
        }

        try {
            Instance instance = toDomainInstance(new IntermediateInstanceProfile(serviceInstance, metadata))
                    .withDiscoveryKey(key);
            return new ProbeOutcome.Success(new DiscoveredInstanceProfile(instance, metadata));
        } catch (IllegalArgumentException e) {
            return rejected("conversion to the internal representation failed: " + e.getMessage());
        }
    }

    private static ProbeOutcome.Failure rejected(String reason) {
        return new ProbeOutcome.Failure(ProbeVerdict.REJECTED, 200, null, reason);
    }

    private void logFailure(InstanceKey key, ProbeOutcome.Failure failure, int failures) {
        switch (failure.verdict()) {
            // Logged once per backoff period, since the instance is not probed again until then.
            case REJECTED -> logger.warn("Instance {} skipped: {}", key, failure.reason());
            case PERMANENT ->
                logger.info(
                        "Instance {} is not a manageable service (status {}): {}",
                        key,
                        failure.status(),
                        failure.reason());
            default ->
                logger.debug(
                        "Probe of {} failed ({}, status {}, {} in a row): {}",
                        key,
                        failure.verdict(),
                        failure.status(),
                        failures,
                        failure.reason());
        }
    }

    private record IntermediateInstanceProfile(ServiceInstance serviceInstance, BasicRegistrationMetadata metadata) {}

    private Instance toDomainInstance(IntermediateInstanceProfile profile) throws IllegalArgumentException {
        ServiceInstance serviceInstance = profile.serviceInstance();

        if (serviceInstance instanceof KubernetesServiceInstance k8sInstance) {
            return instanceFactory.createInstance(
                    k8sInstance.getInstanceId(),
                    k8sInstance.podName(),
                    k8sInstance.getDeploymentAt(),
                    // Instances that are discovered by DiscoveryClient
                    // should not have the heartbeat timestamp
                    null,
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
