package com.nucleonforge.axile.master.service.transport.caches;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#GET_SINGLE_CACHE /caches/{name}} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class CacheByNameEndpointProber extends AbstractEndpointProber<SingleCache> {
    public CacheByNameEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<SingleCache> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.GET_SINGLE_CACHE;
    }
}
