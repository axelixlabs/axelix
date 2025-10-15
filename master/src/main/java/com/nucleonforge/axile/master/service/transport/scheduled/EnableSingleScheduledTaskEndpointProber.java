package com.nucleonforge.axile.master.service.transport.scheduled;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.DiscardingAbstractEndpointProber;

/**
 * {@link DiscardingAbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#ENABLE_SCHEDULED_TASK} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class EnableSingleScheduledTaskEndpointProber extends DiscardingAbstractEndpointProber {
    public EnableSingleScheduledTaskEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.ENABLE_SCHEDULED_TASK;
    }
}
