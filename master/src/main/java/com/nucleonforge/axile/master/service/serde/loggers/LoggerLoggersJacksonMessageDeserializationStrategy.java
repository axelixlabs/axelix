package com.nucleonforge.axile.master.service.serde.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.loggers.LoggerLoggers;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link LoggerLoggers}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class LoggerLoggersJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<LoggerLoggers> {

    public LoggerLoggersJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<LoggerLoggers> supported() {
        return LoggerLoggers.class;
    }
}
