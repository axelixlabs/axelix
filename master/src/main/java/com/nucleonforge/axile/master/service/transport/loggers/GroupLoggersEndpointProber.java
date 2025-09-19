package com.nucleonforge.axile.master.service.transport.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.GroupLoggers;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#LOGGER_GROUP /loggers/{group.name}} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class GroupLoggersEndpointProber extends AbstractEndpointProber<GroupLoggers> {

    public GroupLoggersEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<GroupLoggers> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.LOGGER_GROUP;
    }
}
