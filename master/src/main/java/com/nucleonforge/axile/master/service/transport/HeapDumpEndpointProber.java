package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.HeapDumpMessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#HEAP_DUMP /heapdump} endpoint.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@Service
public class HeapDumpEndpointProber extends AbstractEndpointProber<Resource> {

    public HeapDumpEndpointProber(
            InstanceRegistry instanceRegistry, HeapDumpMessageDeserializationStrategy messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.HEAP_DUMP;
    }
}
