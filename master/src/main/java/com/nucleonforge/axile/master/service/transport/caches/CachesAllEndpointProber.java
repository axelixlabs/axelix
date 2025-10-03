package com.nucleonforge.axile.master.service.transport.caches;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#GET_ALL_CACHES /caches} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class CachesAllEndpointProber extends AbstractEndpointProber<ServiceCaches> {
    public CachesAllEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ServiceCaches> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.GET_ALL_CACHES;
    }
}
