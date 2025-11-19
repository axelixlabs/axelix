package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#THREAD_DUMP /threaddump} endpoint.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Service
public class ThreadDumpEndpointProber extends AbstractEndpointProber<ThreadDumpFeed> {

    protected ThreadDumpEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ThreadDumpFeed> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.THREAD_DUMP;
    }
}
