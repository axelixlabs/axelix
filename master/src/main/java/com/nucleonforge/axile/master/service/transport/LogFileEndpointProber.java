package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.LogFileMessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#LOG_FILE /logfile} endpoint.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Service
public class LogFileEndpointProber extends AbstractEndpointProber<Resource> {

    public LogFileEndpointProber(
            InstanceRegistry instanceRegistry, LogFileMessageDeserializationStrategy messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.LOG_FILE;
    }
}
