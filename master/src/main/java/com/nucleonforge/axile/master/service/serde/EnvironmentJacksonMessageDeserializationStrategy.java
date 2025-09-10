package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link EnvironmentFeed}.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
@Component
public class EnvironmentJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<EnvironmentFeed> {

    public EnvironmentJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<EnvironmentFeed> supported() {
        return EnvironmentFeed.class;
    }
}
