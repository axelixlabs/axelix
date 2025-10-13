package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ServiceScheduledTasks}.
 *
 * @author SergeyCherkasov
 */
@Component
public class ScheduledTasksJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ServiceScheduledTasks> {

    public ScheduledTasksJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ServiceScheduledTasks> supported() {
        return ServiceScheduledTasks.class;
    }
}
