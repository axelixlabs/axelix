package com.nucleonforge.axile.master.service.transport.metrics;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * Endpoint prober for the {@link ActuatorEndpoints#METRICS_GROUP} API.
 *
 * @author Mikhail Polivakha
 */
@Component
public class GetMetricsGroupsEndpointProber extends AbstractEndpointProber<MetricsGroupsFeed> {

    protected GetMetricsGroupsEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<MetricsGroupsFeed> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.METRICS_GROUP;
    }
}
