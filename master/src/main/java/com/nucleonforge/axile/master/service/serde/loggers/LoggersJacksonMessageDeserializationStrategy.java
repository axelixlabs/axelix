package com.nucleonforge.axile.master.service.serde.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.master.service.serde.JacksonMessageDeserializationStrategy;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ServiceLoggers}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class LoggersJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ServiceLoggers> {

    public LoggersJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ServiceLoggers> supported() {
        return ServiceLoggers.class;
    }
}
