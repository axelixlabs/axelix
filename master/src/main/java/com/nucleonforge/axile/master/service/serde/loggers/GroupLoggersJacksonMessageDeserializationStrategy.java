package com.nucleonforge.axile.master.service.serde.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.loggers.GroupLoggers;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link GroupLoggers}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class GroupLoggersJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<GroupLoggers> {

    public GroupLoggersJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<GroupLoggers> supported() {
        return GroupLoggers.class;
    }
}
