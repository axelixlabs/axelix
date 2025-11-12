package com.nucleonforge.axile.master.service.transport.metrics;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.metrics.MetricsList;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * Endpoint prober for the {@link ActuatorEndpoints#METRICS} API.
 *
 * @author Mikhail Polivakha
 */
@Component
public class GetAllMetricsEndpointProber extends AbstractEndpointProber<MetricsList> {

    protected GetAllMetricsEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<MetricsList> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.METRICS;
    }
}
