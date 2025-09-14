package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ConfigpropsFeed}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class ConfigpropsJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ConfigpropsFeed> {

    public ConfigpropsJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ConfigpropsFeed> supported() {
        return ConfigpropsFeed.class;
    }
}
