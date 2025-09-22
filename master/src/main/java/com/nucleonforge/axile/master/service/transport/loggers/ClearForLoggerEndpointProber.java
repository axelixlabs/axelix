package com.nucleonforge.axile.master.service.transport.loggers;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.DiscardingAbstractEndpointProber;

/**
 * {@link DiscardingAbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#CLEAR_FOR_LOGGER /loggers/{logger.name}} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ClearForLoggerEndpointProber extends DiscardingAbstractEndpointProber {

    public ClearForLoggerEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.CLEAR_FOR_LOGGER;
    }
}
