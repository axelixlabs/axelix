package com.nucleonforge.axile.master.service.transport.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.LoggerLoggers;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ONE_LOGGER /loggers/{logger.name}} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class OneLoggerEndpointProber extends AbstractEndpointProber<LoggerLoggers> {

    public OneLoggerEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<LoggerLoggers> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ONE_LOGGER;
    }
}
