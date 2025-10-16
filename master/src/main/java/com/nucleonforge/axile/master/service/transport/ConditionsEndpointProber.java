package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#CONDITIONS /conditions} endpoint.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@Service
public class ConditionsEndpointProber extends AbstractEndpointProber<ConditionsFeed> {

    public ConditionsEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ConditionsFeed> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.CONDITIONS;
    }
}
