package com.nucleonforge.axile.master.service.transport.metrics;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.metrics.MetricProfileByTags;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * Endpoint prober for the {@link ActuatorEndpoints#SINGLE_METRIC_BY_TAGS} API.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Service
public class GetSingleMetricByTagsProfileEndpointProber extends AbstractEndpointProber<MetricProfileByTags> {

    protected GetSingleMetricByTagsProfileEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<MetricProfileByTags> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.SINGLE_METRIC_BY_TAGS;
    }
}
