package com.nucleonforge.axile.master.service.transport.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ALL_LOGGERS /loggers} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class AllLoggersEndpointProber extends AbstractEndpointProber<ServiceLoggers> {

    public AllLoggersEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ServiceLoggers> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ALL_LOGGERS;
    }
}
