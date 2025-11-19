package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ThreadDumpFeed}.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@Component
public class ThreadDumpJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ThreadDumpFeed> {

    public ThreadDumpJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ThreadDumpFeed> supported() {
        return ThreadDumpFeed.class;
    }
}
