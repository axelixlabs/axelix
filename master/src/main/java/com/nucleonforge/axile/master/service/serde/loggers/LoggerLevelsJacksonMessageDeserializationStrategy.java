package com.nucleonforge.axile.master.service.serde.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link LoggerLevels}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class LoggerLevelsJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<LoggerLevels> {

    public LoggerLevelsJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<LoggerLevels> supported() {
        return LoggerLevels.class;
    }
}
