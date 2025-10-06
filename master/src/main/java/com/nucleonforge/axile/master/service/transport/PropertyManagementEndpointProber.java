package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link DiscardingAbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#PROPERTY_MANAGEMENT /property-management} endpoint.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@Service
public class PropertyManagementEndpointProber extends DiscardingAbstractEndpointProber {

    public PropertyManagementEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.PROPERTY_MANAGEMENT;
    }
}
