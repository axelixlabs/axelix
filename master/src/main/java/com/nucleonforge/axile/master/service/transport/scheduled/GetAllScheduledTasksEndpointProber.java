package com.nucleonforge.axile.master.service.transport.scheduled;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.AbstractEndpointProber;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#SCHEDULED_TASKS} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class GetAllScheduledTasksEndpointProber extends AbstractEndpointProber<ServiceScheduledTasks> {

    public GetAllScheduledTasksEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ServiceScheduledTasks> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.SCHEDULED_TASKS;
    }
}
