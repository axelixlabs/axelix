package com.nucleonforge.axile.master.service.transport.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#SET_FOR_LOGGER_GROUP /loggers/{group.name}} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class SetForLoggerGroupEndpointProber extends AbstractEndpointProber<Object> {

    public SetForLoggerGroupEndpointProber(
            InstanceRegistry instanceRegistry, MessageDeserializationStrategy<Object> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.SET_FOR_LOGGER_GROUP;
    }
}
