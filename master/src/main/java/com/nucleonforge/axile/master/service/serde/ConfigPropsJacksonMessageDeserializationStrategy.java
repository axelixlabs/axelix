package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link ConfigPropsFeed}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class ConfigPropsJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<ConfigPropsFeed> {

    public ConfigPropsJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<ConfigPropsFeed> supported() {
        return ConfigPropsFeed.class;
    }
}
