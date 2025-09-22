package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ManagedServiceMetadata;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#METADATA} endpoint.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Service
public class ManagedServiceMetadataEndpointProber extends AbstractEndpointProber<ManagedServiceMetadata> {

    public ManagedServiceMetadataEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ManagedServiceMetadata> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.METADATA;
    }
}
