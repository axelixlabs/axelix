package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ProfileMutationResult;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with
 * {@link ActuatorEndpoints#PROFILE_MANAGEMENT /profile-management} endpoint.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@Service
public class ProfileManagementEndpointProber extends AbstractEndpointProber<ProfileMutationResult> {

    public ProfileManagementEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ProfileMutationResult> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.PROFILE_MANAGEMENT;
    }
}
