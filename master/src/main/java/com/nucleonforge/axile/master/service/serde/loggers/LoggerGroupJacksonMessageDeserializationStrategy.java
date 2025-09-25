package com.nucleonforge.axile.master.service.serde.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link LoggerGroup}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class LoggerGroupJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<LoggerGroup> {

    public LoggerGroupJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<LoggerGroup> supported() {
        return LoggerGroup.class;
    }
}
