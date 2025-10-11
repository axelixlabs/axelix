package com.nucleonforge.axile.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axile.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#SCHEDULED_TASKS /scheduledtasks} endpoint.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ScheduledTasksEndpointProber extends AbstractEndpointProber<ServiceScheduledTasks> {

    public ScheduledTasksEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ServiceScheduledTasks> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.SCHEDULED_TASKS;
    }
}
