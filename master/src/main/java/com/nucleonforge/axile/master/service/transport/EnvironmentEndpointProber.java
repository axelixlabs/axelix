package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ENV /env} endpoint.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
@Service
public class EnvironmentEndpointProber extends AbstractEndpointProber<EnvironmentFeed> {

    public EnvironmentEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<EnvironmentFeed> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ENV;
    }
}
