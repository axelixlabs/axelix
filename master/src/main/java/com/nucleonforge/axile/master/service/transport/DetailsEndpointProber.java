package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#DETAILS /axile-info} endpoint.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
@Service
public class DetailsEndpointProber extends AbstractEndpointProber<AxileDetails> {

    public DetailsEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<AxileDetails> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.DETAILS;
    }
}
