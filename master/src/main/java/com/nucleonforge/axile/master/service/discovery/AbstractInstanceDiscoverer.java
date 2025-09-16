package com.nucleonforge.axile.master.service.discovery;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import com.nucleonforge.axile.common.domain.Instance;

/**
 * Abstract implementation of {@link InstancesDiscoverer}.
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractInstanceDiscoverer implements InstancesDiscoverer {

    private final DiscoveryClient discoveryClient;
    private final Logger log;

    public AbstractInstanceDiscoverer(DiscoveryClient discoveryClient, Logger log) {
        this.discoveryClient = discoveryClient;
        this.log = log;
    }

    @Override
    public @NonNull Set<@NonNull Instance> discover() {
        List<String> serviceIds = discoveryClient.getServices();

        if (CollectionUtils.isEmpty(serviceIds)) {
            log.error(
                    """
                Despite the auto-discovery was enabled, the {} did not found any result.
                That is almost certainly not the intended behavior. Please, revisit your configuration
                """,
                    this.getClass().getSimpleName());
            return Set.of();
        }

        Set<Instance> result = new HashSet<>();

        for (String serviceId : serviceIds) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

            if (!CollectionUtils.isEmpty(instances)) {
                result.addAll(instances.stream()
                        .filter(Objects::nonNull)
                        .map(this::toDomainInstance)
                        .collect(Collectors.toSet()));
            }
        }

        return result;
    }

    public abstract Instance toDomainInstance(ServiceInstance instance);
}
