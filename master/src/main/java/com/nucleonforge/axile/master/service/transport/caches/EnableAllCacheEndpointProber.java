package com.nucleonforge.axile.master.service.transport.caches;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;
import com.nucleonforge.axile.master.service.transport.DiscardingAbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ENABLE_ALL_CACHE /caches} endpoint.
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@Service
public class EnableAllCacheEndpointProber extends DiscardingAbstractEndpointProber {

    public EnableAllCacheEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ENABLE_ALL_CACHE;
    }
}
