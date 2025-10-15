package com.nucleonforge.axile.master.service.transport.scheduled;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.DiscardingAbstractEndpointProber;

/**
 * {@link DiscardingAbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#DISABLE_SCHEDULED_TASK} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class DisableSingleScheduledTaskEndpointProber extends DiscardingAbstractEndpointProber {
    public DisableSingleScheduledTaskEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.DISABLE_SCHEDULED_TASK;
    }
}
