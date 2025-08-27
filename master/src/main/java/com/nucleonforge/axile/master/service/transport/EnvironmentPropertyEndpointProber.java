package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ENV /env/property/{propertyName}} endpoint.
 *
 * @since 28.08.2025
 * @author Nikita Kirillov
 */
@Service
public class EnvironmentPropertyEndpointProber extends AbstractEndpointProber<EnvironmentProperty> {

    public EnvironmentPropertyEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<EnvironmentProperty> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ENV_PROPERTY;
    }
}
