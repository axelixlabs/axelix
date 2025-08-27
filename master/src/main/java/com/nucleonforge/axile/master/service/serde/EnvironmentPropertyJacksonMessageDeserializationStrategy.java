package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link EnvironmentProperty}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
@Component
public class EnvironmentPropertyJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<EnvironmentProperty> {

    public EnvironmentPropertyJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<EnvironmentProperty> supported() {
        return EnvironmentProperty.class;
    }
}
