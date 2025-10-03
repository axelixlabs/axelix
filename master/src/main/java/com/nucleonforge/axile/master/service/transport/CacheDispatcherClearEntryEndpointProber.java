package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#CACHE_DISPATCHER_CLEAR_ENTRY
 * /cache-dispatcher/{cacheManagerName}/clear} endpoint.
 *
 * @since 02.10.2025
 * @author Nikita Kirillov
 */
@Service
public class CacheDispatcherClearEntryEndpointProber extends AbstractEndpointProber<CacheDispatcherClearResult> {

    public CacheDispatcherClearEntryEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<CacheDispatcherClearResult> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.CACHE_DISPATCHER_CLEAR_ENTRY;
    }
}
