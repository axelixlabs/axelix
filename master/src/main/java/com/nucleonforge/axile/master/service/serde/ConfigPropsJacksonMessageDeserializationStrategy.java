package com.nucleonforge.axile.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;

/**
 * {@link JacksonMessageDeserializationStrategy} for {@link AxileConfigPropsFeed}.
 *
 * @author Sergey Cherkasov
 */
@Component
public class ConfigPropsJacksonMessageDeserializationStrategy
        extends JacksonMessageDeserializationStrategy<AxileConfigPropsFeed> {

    public ConfigPropsJacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public @NonNull Class<AxileConfigPropsFeed> supported() {
        return AxileConfigPropsFeed.class;
    }
}
