package com.nucleonforge.axile.master.service.transport.caches;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;
import com.nucleonforge.axile.master.service.transport.DiscardingAbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ENABLE_CACHE_MANAGER /caches} endpoint.
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@Service
public class EnableCacheManagerEndpointProber extends DiscardingAbstractEndpointProber {

    public EnableCacheManagerEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ENABLE_CACHE_MANAGER;
    }
}
